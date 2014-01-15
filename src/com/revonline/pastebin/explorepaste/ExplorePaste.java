package com.revonline.pastebin.explorepaste;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.revonline.pastebin.MyActivity;
import com.revonline.pastebin.R;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 01/12/13
 * Time: 1.45
 * To change this template use File | Settings | File Templates.
 */
public class ExplorePaste extends Activity
{
    public static final String FLAG_EXTRA_PASTE_URL = "PASTE.EXTRA.PASTE_URL";
    private String pasteKey;
    private TextView textView;
    private boolean downloaded = false;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paste);

        if (!MyActivity.apiLower11)
        {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pasteKey = getIntent().getStringExtra(FLAG_EXTRA_PASTE_URL);
        Log.d(MyActivity.DEBUG_TAG, "pasteKey==" + pasteKey);
        setTitle("Paste - [" + pasteKey + "]");

        textView = (TextView) findViewById(R.id.codeview);

        downloaded = (savedInstanceState != null) && savedInstanceState.getBoolean("downloaded");

        if (!downloaded) new DownloadRAW().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("downloaded", downloaded);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.explorepastemenu, menu);
//        MenuItem item = menu.findItem(R.id.sharepaste);
//        shareActionProvider = (ShareActionProvider) item.getActionProvider();

        return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent intent;
        switch (item.getItemId())
        {
            case R.id.forkpaste:
                intent = new Intent(this, MyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(MyActivity.EXTRA_FLAG_FORK, textView.getText().toString());
                startActivity(intent);
                break;
            case R.id.openinbrowser:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://pastebin.com/" + pasteKey));
                startActivity(intent);
                break;
        }
        return super.onMenuItemSelected(featureId, item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    class DownloadRAW extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();    //To change body of overridden methods use File | Settings | File Templates.

            alertDialog = new ProgressDialog(ExplorePaste.this);
            alertDialog.setMessage(ExplorePaste.this.getString(R.string.waitdownload));
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP)
                    {
                        finish();
                        return true;
                    }

                    return false;
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String finalResponse = null;

            try{
                HttpClient client = new DefaultHttpClient();
                HttpResponse response;
                HttpGet httpGet = new HttpGet("http://pastebin.com/raw.php?i=" + pasteKey);

                response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    outputStream.close();

                    finalResponse = outputStream.toString();
                }
                else
                {
                    response.getEntity().getContent().close();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            return finalResponse;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);    //To change body of overridden methods use File | Settings | File Templates.

            downloaded = true;
            if (s != null)
            {
                textView.setText(s);
            }
            //no paste here

            alertDialog.dismiss();
        }
    }
}