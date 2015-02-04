package com.revonline.pastebin.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;
import com.revonline.pastebin.ErrorMessages;
import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.R;
import com.revonline.pastebin.ShareCodeActivity;
import com.revonline.pastebin.SpecialKeys;
import com.revonline.pastebin.adapters.PastesListAdapter;
import com.revonline.pastebin.database.PasteDBHelper;
import com.revonline.pastebin.explorepaste.ExplorePaste;
import com.revonline.pastebin.xml.XMLHandler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
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
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * THIS CODE IS VERY VERY VERY VERY VERY BAD SHOULD BE IMPROVED
 *
 * USERACTIVITY SHOULD'T MANAGE LOGIN ACTIONS, USER.JAVA SHOULD PROVIDE LOGIN(STRING,STRING) METHOD
 */
public class UserActivity extends Activity {

  private static final String EXTRA_FORCE_LOGGED_VIEW = "EXTRA_FORCE_LOGGED_VIEW";
  private User user;
  private EditText username;
  private EditText password;
  private Button loginButton;
  private ListView pastesList;
  private PastesListAdapter adapter;
  private TextView listViewEmptyText;
  private boolean showLocalPastes;
  private DownloadUserPastes downloadUserPastesTask;
  private MenuItem showLocalPastesMenuItem;

  @Override
  public void onCreate(Bundle savedInstanceHere) {
    super.onCreate(savedInstanceHere);

    if (!ShareCodeActivity.apiLower11) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    user = new User(this);
    final Bundle extras = getIntent().getExtras();

    boolean forceLoggedView = false;
    if (extras != null) {
      forceLoggedView = extras.getBoolean(EXTRA_FORCE_LOGGED_VIEW);
    }

    if (!user.isLogged() && !forceLoggedView) {
      setContentView(R.layout.loginwindow);

      username = (EditText) findViewById(R.id.username);
      password = (EditText) findViewById(R.id.password);
      loginButton = (Button) findViewById(R.id.loginbutton);
    } else {
      setContentView(R.layout.iopastes);
      // ID: mypastes
      pastesList = (ListView) findViewById(R.id.mypastes);

      adapter = new PastesListAdapter(this);

      pastesList.setAdapter(adapter);
      pastesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          Intent intent = new Intent(parent.getContext(), ExplorePaste.class);
          //Log.d(ShareCodeActivity.DEBUG_TAG, "parent.getItemIdAtPosition(position) => " + parent.getItemAtPosition(position));
          intent.putExtra(ExplorePaste.EXTRA_PASTE_INFO,
                          (Parcelable) parent.getItemAtPosition(position));
          //intent.putExtra(ExplorePaste.EXTRA_PASTE_INFO, ((PasteInfo) parent.getItemAtPosition(position)).getPasteKey());
          startActivity(intent);
        }
      });

      pastesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
          final PasteInfo pasteInfo = (PasteInfo) parent.getItemAtPosition(position);

          new AlertDialog.Builder(UserActivity.this)
              .setTitle(R.string.delete_paste)
              .setMessage(R.string.delete_paste_in_local_and_pastebin)
              .setPositiveButton(R.string.pastebin_and_phone, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                  final PasteDBHelper pasteDBHelper = new PasteDBHelper(UserActivity.this);
                  final boolean localRemoveSuccess = deleteLocalPaste(pasteDBHelper, pasteInfo);
                  deleteRemovePaste(pasteDBHelper, pasteInfo);

                  Toast.makeText
                      (
                          UserActivity.this,
                          getString(
                              R.string.paste_deleted_local_memory,
                              localRemoveSuccess ? getString(R.string.yes) : getString(R.string.no)
                          ),
                          Toast.LENGTH_SHORT
                      ).show();
                }
              })
              .setNegativeButton(R.string.phone_only, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                  final PasteDBHelper pasteDBHelper = new PasteDBHelper(UserActivity.this);
                  final boolean success = deleteLocalPaste(pasteDBHelper, pasteInfo);

                  if (!success) {
                    Toast.makeText(UserActivity.this, R.string.paste_not_saved_in_memory, Toast.LENGTH_SHORT).show();
                    return;
                  }

                  Toast.makeText(UserActivity.this, getString(R.string.paste_deleted_local_memory, getString(R.string.yes)), Toast.LENGTH_SHORT).show();
                  adapter.removePaste(pasteInfo);
                }
              })
              .setNeutralButton(R.string.close, null)
              .show();

          return true;
        }
      });

      listViewEmptyText = (TextView) findViewById(R.id.empty);
      pastesList.setEmptyView(listViewEmptyText);

      if (user.isLogged()) {
        new DownloadUserPastes().execute();
      } else {
        showUserPastes();
        showLocalPastes = true;
      }
    }
  }

  private void deleteRemovePaste(final PasteDBHelper pasteDBHelper, final PasteInfo pasteInfo) {
    new DeletePasteRequest().execute(pasteInfo);
  }

  private boolean deleteLocalPaste(final PasteDBHelper pasteDBHelper, final PasteInfo pasteInfo) {
    final int sqlId = pasteInfo.getSqlID();

    // -1 means that I don't have this paste in the DB
    // but it could still exists in pastebin
    if (sqlId == -1) {
      return false;
    }

    return pasteDBHelper.deletePaste(pasteInfo.getSqlID());
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.userlogout:
        if (!user.isLogged()) {
          return super.onMenuItemSelected(featureId, item);
        }

        user.logout();
        reloadWindow();
        break;
      case R.id.userLocalPastes:
        showLocalPastes = !showLocalPastes;
        updateMenuItemLocalPastesText();
        updatePastesListView();
        break;
    }

    return super.onMenuItemSelected(featureId, item);
  }

  private void updateMenuItemLocalPastesText() {
    showLocalPastesMenuItem.setTitle(!showLocalPastes ? R.string.localpastes : R.string.accountpastes);
  }

  private void updatePastesListView() {
    if (showLocalPastes) {
      if (downloadUserPastesTask != null) {
        downloadUserPastesTask.cancel(true);
        downloadUserPastesTask = null;
      }

      if (!user.isLogged()) {
        Intent intent = getIntent();

        intent.putExtra(EXTRA_FORCE_LOGGED_VIEW, true);
        finish();

        startActivity(intent);
        return;
      }

      showUserPastes();
    } else {
      // yyeeeeaaaaaah it's bad, but for now it works >_<
      // i wrote this code YEARS AGO! so don't blame me
      //
      if (downloadUserPastesTask != null) {
        return;
      }

      if (!user.isLogged()) {
        Intent intent = getIntent();
        intent.putExtra(EXTRA_FORCE_LOGGED_VIEW, false);
        finish();
        startActivity(intent);
        return;
      }

      adapter.setPasteInfoList(Collections.<PasteInfo>emptyList());

      downloadUserPastesTask = new DownloadUserPastes();
      downloadUserPastesTask.execute();
    }
  }

  private void showUserPastes() {
    adapter.setPasteInfoList(new PasteDBHelper(this).getAllPastes());
    adapter.notifyDataSetChanged();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.offlinepastesmenu, menu);

    if (user.isLogged()) {
      getMenuInflater().inflate(R.menu.iomenu, menu);
    }

    showLocalPastesMenuItem = menu.findItem(R.id.userLocalPastes);
    updateMenuItemLocalPastesText();

    return super.onCreateOptionsMenu(menu);
  }

  @SuppressLint("NewApi")
  private void reloadWindow() {
    if (!ShareCodeActivity.apiLower11) {
      recreate();
    } else {
      Intent intent = getIntent();
      finish();
      startActivity(intent);
    }
  }

  public void tryLogin(View view) {
    if (username == null || password == null) {
      return;
    }

    String name = username.getText().toString();
    String password = this.password.getText().toString();

    Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
    boolean failed = false;

    if (name.length() == 0) {
      username.startAnimation(shake);
      failed = true;
    }

    if (password.length() == 0) {
      this.password.startAnimation(shake);
      failed = true;
    }

    if (failed) {
      return;
    }

    this.username.setEnabled(false);
    this.password.setEnabled(false);
    this.loginButton.setEnabled(false);

    new LoginTask().execute(name, password);
  }

  class DeletePasteRequest extends AsyncTask<PasteInfo, Void, NetworkDeleteResult> {
    private ProgressDialog progressDialog;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      progressDialog = new ProgressDialog(UserActivity.this);
      progressDialog.setMessage(getString(R.string.delete_in_progress));
      // we cannot implement "Cancel" in a safe way yet
//      progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.close), new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(final DialogInterface dialog, final int which) {
//          cancel(true);
//        }
//      });
      progressDialog.setCancelable(false);
      progressDialog.setIndeterminate(true);
      progressDialog.show();
    }

    @Override
    protected NetworkDeleteResult doInBackground(final PasteInfo... params) {
      if (!user.isLogged()) {
        return new NetworkDeleteResult(false, getString(R.string.no_logged), params[0]);
      }

      final PasteInfo pasteInfo = params[0];

      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost("http://pastebin.com/api/api_post.php");
      HttpResponse response;
      String bodyresponse;
      List<BasicNameValuePair> pairs = new ArrayList<>();

      pairs.add(new BasicNameValuePair("api_option", "delete"));
      pairs.add(new BasicNameValuePair("api_user_key", user.getUserKey()));
      pairs.add(new BasicNameValuePair("api_dev_key", SpecialKeys.DEV_KEY));
      pairs.add(new BasicNameValuePair("api_paste_key", pasteInfo.getPasteKey()));

      try {
        post.setEntity(new UrlEncodedFormEntity(pairs));

        if (isCancelled()) {
          return new NetworkDeleteResult(false, getString(R.string.request_deleted), pasteInfo);
        }

        response = client.execute(post);
        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          response.getEntity().writeTo(outputStream);
          outputStream.close();
          bodyresponse = outputStream.toString();
          Log.d(ShareCodeActivity.DEBUG_TAG, "bodyresponse == " + bodyresponse);

          final boolean success = "Paste Removed".equals(bodyresponse);
          return new NetworkDeleteResult(success, success ? "" : getString(ErrorMessages.errors.get(bodyresponse)), pasteInfo);
        } else {
          response.getEntity().getContent().close();
        }
      } catch (IOException e) {
        Log.e(ShareCodeActivity.DEBUG_TAG, "when deleting a paste " + pasteInfo, e);
      }

      return new NetworkDeleteResult(false, getString(R.string.nointernet), pasteInfo);
    }

    @Override
    protected void onPostExecute(final NetworkDeleteResult networkDeleteResult) {
      super.onPostExecute(networkDeleteResult);
      progressDialog.hide();

      if (isFinishing() || isCancelled()) {
        return;
      }

      handleDeleteNetworkRequestResponse(networkDeleteResult);
    }
  }

  private void handleDeleteNetworkRequestResponse(final NetworkDeleteResult result) {
    if (result.isSuccess()) {
      Toast.makeText(this, getString(R.string.paste_deleted_pastebin), Toast.LENGTH_SHORT).show();
      adapter.removePaste(result.getPasteInfo());
      return;
    }

    Toast.makeText(
        this,
        getString(R.string.unable_to_delete_paste_from_remote, result.getMessage()),
        Toast.LENGTH_LONG
    ).show();
  }

  class DownloadUserPastes extends AsyncTask<Void, Void, String> {
//        ProgressDialog alertDialog;

    List<PasteInfo> pasteInfos;    @Override
    protected void onPreExecute() {
      super
        .onPreExecute();    //To change body of overridden methods use File | Settings | File Templates.

      listViewEmptyText.setText(R.string.waitdownloadlist);
    }
    DialogInterface.OnClickListener retry = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        new DownloadUserPastes().execute();
      }
    };
    DialogInterface.OnClickListener close = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        finish();
      }
    };

    @Override
    protected String doInBackground(Void... params) {
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost("http://pastebin.com/api/api_post.php");
      HttpResponse response;
      String bodyresponse = null;
      List<BasicNameValuePair> pairs = new ArrayList<>();

      pairs.add(new BasicNameValuePair("api_option", "list"));
      pairs.add(new BasicNameValuePair("api_user_key", user.getUserKey()));
      pairs.add(new BasicNameValuePair("api_dev_key", SpecialKeys.DEV_KEY));
      pairs.add(new BasicNameValuePair("api_results_limit", "100"));

      try {
        post.setEntity(new UrlEncodedFormEntity(pairs));

        response = client.execute(post);
        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          response.getEntity().writeTo(outputStream);
          outputStream.close();
          bodyresponse = outputStream.toString();
          Log.d(ShareCodeActivity.DEBUG_TAG, "bodyresponse == " + bodyresponse);

          SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
          SAXParser parser = saxParserFactory.newSAXParser();
          XMLReader reader = parser.getXMLReader();
          XMLHandler handler = new XMLHandler(UserActivity.this);
          reader.setContentHandler(handler);
          reader.parse(new InputSource(new StringReader("<root>" + bodyresponse + "</root>")));

          pasteInfos = handler.data;
        } else {
          response.getEntity().getContent().close();
        }
      } catch (UnsupportedEncodingException | SAXException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      } catch (IOException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      } catch (ParserConfigurationException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }

      return bodyresponse;  //To change body of implemented methods use File | Settings | File Templates.
    }    @Override
    protected void onPostExecute(String xml) {
      super.onPostExecute(
        xml);    //To change body of overridden methods use File | Settings | File Templates.
      Log.d(ShareCodeActivity.DEBUG_TAG, "pasteInfos = " + pasteInfos);
      downloadUserPastesTask = null;

      if (isCancelled()) {
        return;
      }

//            alertDialog.dismiss();
      if (pasteInfos != null /*&& pasteInfos.size() > 0*/) {
        adapter.setPasteInfoList(pasteInfos);
        listViewEmptyText.setText(R.string.norecords);
      } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);

        Log.d(ShareCodeActivity.DEBUG_TAG, "xml => " + xml);
        if (ErrorMessages.errors.containsKey(xml)) {
          builder.setMessage(
            getString(R.string.msgerrore, "(" + getString(ErrorMessages.errors.get(xml)) + ")"));
        } else {
          builder.setMessage(R.string.nointernet);
        }

        // i do in this way cuz i think it can bug
        builder.setPositiveButton(R.string.retry, retry);
        builder.setNegativeButton(R.string.close, close);
        builder.show();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // LoginTask section; ignore it
  // ToDo: I think login code should be inside User.java but for now..

  class LoginTask extends AsyncTask<String, Void, String> {

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

      ArrayList<BasicNameValuePair> pair = new ArrayList<>();
      pair.add(new BasicNameValuePair("api_dev_key", SpecialKeys.DEV_KEY));

      try {
        pair.add(new BasicNameValuePair("api_user_name", URLEncoder.encode(name, "ISO-8859-1")));
        pair.add(
          new BasicNameValuePair("api_user_password", URLEncoder.encode(password, "ISO-8859-1")));

        post.setEntity(new UrlEncodedFormEntity(pair));

        response = client.execute(post);
        StatusLine line = response.getStatusLine();

        if (line.getStatusCode() == HttpStatus.SC_OK) {
          // OK
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          response.getEntity().writeTo(outputStream);
          outputStream.close();
          resp = outputStream.toString();
        }
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return resp;
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);

      if (isFinishing()) {
        return;
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          repeatLogin();
        }
      });

      if (s == null) {
        builder.setMessage(R.string.nointernet);
        builder.setPositiveButton(R.string.retry, retry);
        builder.setNegativeButton(R.string.close, close);
      } else {
        if (ErrorMessages.errors.containsKey(s)) {
          builder.setMessage(ErrorMessages.errors.get(s));
          builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                            username.setText(null);
              repeatLogin();
            }
          });

          builder.setNegativeButton(R.string.close, close);
        } else {
          user.setUserKey(s);
          user.setUserName(username.getText().toString());

//                    builder.setMessage(R.string.loginok);
//                    builder.setPositiveButton(R.string.continua, new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            reloadWindow();
//                        }
//                    });
          reloadWindow();
          return;
        }
      }

      builder.show();
    }

    // used when i need to reenable buttons
    private void repeatLogin() {
      password.setText(null);

      username.setEnabled(true);
      password.setEnabled(true);
      loginButton.setEnabled(true);
    }
  }

  private static class NetworkDeleteResult {
    private final boolean success;
    private final String message;
    private final PasteInfo pasteInfo;

    public NetworkDeleteResult(final boolean success, final String message, final PasteInfo pasteInfo) {
      this.success = success;
      this.message = message;
      this.pasteInfo = pasteInfo;
    }

    public boolean isSuccess() {
      return success;
    }

    public String getMessage() {
      return message;
    }

    public PasteInfo getPasteInfo() {
      return pasteInfo;
    }
  }
}
