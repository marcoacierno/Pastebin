package com.revonline.pastebin.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.revonline.pastebin.ErrorMessages;
import com.revonline.pastebin.MyActivity;
import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.R;
import com.revonline.pastebin.SpecialKeys;
import com.revonline.pastebin.adapters.PastesListAdapter;
import com.revonline.pastebin.explorepaste.ExplorePaste;
import com.revonline.pastebin.xml.XMLHandler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Marco on 13/12/13.
 */

/**
 * THIS CODE IS VERY VERY VERY VERY VERY BAD
 * SHOULD BE IMPROVED
 *
 * USERACTIVITY SHOULD'T MANAGE LOGIN ACTIONS, USER.JAVA SHOULD PROVIDE LOGIN(STRING,STRING) METHOD
 */
public class UserActivity extends Activity
{
    private User user;
    private EditText username;
    private EditText password;
    private ListView pastesList;
    private PastesListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceHere)
    {
        super.onCreate(savedInstanceHere);

        if (!MyActivity.apiLower11)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        user = new User(this);

        if (!user.isLogged())
        {
            setContentView(R.layout.loginwindow);

            username = (EditText) findViewById(R.id.username);
            password = (EditText) findViewById(R.id.password);
        }
        else
        {
            setContentView(R.layout.iopastes);
            // ID: mypastes
            pastesList = (ListView) findViewById(R.id.mypastes);

            adapter = new PastesListAdapter(this);
            pastesList.setAdapter(adapter);
            pastesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(parent.getContext(), ExplorePaste.class);
                    intent.putExtra(ExplorePaste.FLAG_EXTRA_PASTE_URL, ((PasteInfo) parent.getItemAtPosition(position)).getPasteKey());
                    startActivity(intent);
                }
            });
            pastesList.setEmptyView(findViewById(R.id.empty));
            // ToDo: Implement "long click" => delete paste
//            pastesList.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    return false;
//                }
//            });
            new DownloadPastes().execute();
        }
    }

    public void tryLogin(View view)
    {
        if (username == null || password == null)
            return;

        String name = username.getText().toString();
        String password = this.password.getText().toString();

        this.username.setEnabled(false);
        this.password.setEnabled(false);

        new LoginTask().execute(name, password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (MyActivity.apiLower11)
//        {
//
//        }
        if (user.isLogged())
        {
            getMenuInflater().inflate(R.menu.iomenu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (user.isLogged())
        {
            switch (item.getItemId())
            {
                case R.id.userlogout:
                    user.logout();
                    reloadWindow();
                    break;
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    // già gestisco di mio il problema quindi..
    @SuppressLint("NewApi")
    private void reloadWindow()
    {
        if (!MyActivity.apiLower11)
            recreate();
        else
        {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DownloadPastes code, ignore it
    class DownloadPastes extends AsyncTask<Void, Void, String>
    {
        ProgressDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();    //To change body of overridden methods use File | Settings | File Templates.
            alertDialog = new ProgressDialog(UserActivity.this);
            alertDialog.setMessage(UserActivity.this.getString(R.string.waitdownloadlist));
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

        List<PasteInfo> pasteInfos;
        @Override
        protected String doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://pastebin.com/api/api_post.php");
            HttpResponse response;
            String bodyresponse = null;
            List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();

            pairs.add(new BasicNameValuePair("api_option", "list"));
            pairs.add(new BasicNameValuePair("api_user_key", user.getUserKey()));
            pairs.add(new BasicNameValuePair("api_dev_key", SpecialKeys.DEV_KEY));
            pairs.add(new BasicNameValuePair("api_results_limit", "100"));

            try
            {
                post.setEntity(new UrlEncodedFormEntity(pairs));

                response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    outputStream.close();
                    bodyresponse = outputStream.toString();
                    Log.d(MyActivity.DEBUG_TAG, "bodyresponse == " + bodyresponse);

                    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                    SAXParser parser = saxParserFactory.newSAXParser();
                    XMLReader reader = parser.getXMLReader();
                    XMLHandler handler = new XMLHandler();
                    reader.setContentHandler(handler);
                    reader.parse(new InputSource(new StringReader("<root>" + bodyresponse + "</root>")));

                    pasteInfos = handler.data;
                }
                else
                {
                    response.getEntity().getContent().close();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClientProtocolException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            return bodyresponse;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void onPostExecute(String xml) {
            super.onPostExecute(xml);    //To change body of overridden methods use File | Settings | File Templates.
            Log.d(MyActivity.DEBUG_TAG, "pasteInfos = " + pasteInfos);

            alertDialog.dismiss();
            if (pasteInfos != null && pasteInfos.size() > 0)
            {
                adapter.setPasteInfoList(pasteInfos);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);

                Log.d(MyActivity.DEBUG_TAG, "xml => " + xml);
                if (ErrorMessages.errors.containsKey(xml))
                {
                    builder.setMessage(getString(R.string.msgerrore, "(" + getString(ErrorMessages.errors.get(xml)) + ")"));
                }
                else
                {
                    builder.setMessage(R.string.nointernet);
                }

                // i do in this way cuz i think it can bug
                builder.setPositiveButton(R.string.retry, retry);
                builder.setNegativeButton(R.string.close, close);
                builder.show();
            }
        }

        DialogInterface.OnClickListener retry = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DownloadPastes().execute();
            }
        };

        DialogInterface.OnClickListener close = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LoginTask section; ignore it
    // ToDo: I think login code should be inside User.java but for now..

    class LoginTask extends AsyncTask<String, Void, String>
    {
        DialogInterface.OnClickListener close = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        DialogInterface.OnClickListener retry = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new LoginTask().execute(username.getText().toString(), password.getText().toString());
            }
        };

        @Override
        protected String doInBackground(String... strings) {
            String name = strings[0];
            String password = strings[1];

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://pastebin.com/api/api_login.php");
            HttpResponse response;
            String resp = null;

            ArrayList<BasicNameValuePair> pair = new ArrayList<BasicNameValuePair>();
            pair.add(new BasicNameValuePair("api_dev_key", SpecialKeys.DEV_KEY));

            try
            {
                pair.add(new BasicNameValuePair("api_user_name", URLEncoder.encode(name, "ISO-8859-1")));
                pair.add(new BasicNameValuePair("api_user_password", URLEncoder.encode(password, "ISO-8859-1")));

                post.setEntity(new UrlEncodedFormEntity(pair));

                response = client.execute(post);
                StatusLine line = response.getStatusLine();

                if (line.getStatusCode() == HttpStatus.SC_OK)
                {
                    // OK
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    outputStream.close();
                    resp = outputStream.toString();
                }
            }
            catch(UnsupportedEncodingException e)
            {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);

            if (s == null)
            {
                builder.setMessage(R.string.nointernet);
                builder.setPositiveButton(R.string.retry, retry);
                builder.setNegativeButton(R.string.close, close);
            }
            else
            {
                if (ErrorMessages.errors.containsKey(s))
                {
                    builder.setMessage(ErrorMessages.errors.get(s));
                    builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            username.setText(null);
                            password.setText(null);

                            username.setEnabled(true);
                            password.setEnabled(true);
                        }
                    });

                    builder.setNegativeButton(R.string.close, close);
                }
                else
                {
                    user.setUserKey(s);
                    user.setUserName(username.getText().toString());

                    builder.setMessage(R.string.loginok);
                    builder.setPositiveButton(R.string.continua, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reloadWindow();
                        }
                    });
                }
            }

            builder.show();
        }
    }
}
