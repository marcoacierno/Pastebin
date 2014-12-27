package com.revonline.pastebin.explorepaste;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.R;
import com.revonline.pastebin.ShareCodeActivity;
import com.revonline.pastebin.notification.CompatibleNotification;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 01/12/13 Time: 1.45 To change this template use
 * File | Settings | File Templates.
 */
public class ExplorePaste extends Activity {

  public static final String EXTRA_PASTE_INFO = "PASTE.EXTRA.PASTE_URL";
  private String pasteKey;
  private PasteInfo paste;
  private TextView textView;
  private boolean downloaded = false;
  private boolean downloadConfirm = false;
  private NotificationManager manager;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.paste);

    paste = getIntent().getParcelableExtra(EXTRA_PASTE_INFO);
    Log.d(ShareCodeActivity.DEBUG_TAG, "paste obj: " + paste);
    pasteKey = paste.getPasteKey();

    if (!ShareCodeActivity.apiLower11) {
      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setSubtitle(getString(R.string.language, paste.getPasteLanguage()));
    }

    Log.d(ShareCodeActivity.DEBUG_TAG, "pasteKey==" + pasteKey);
    String
      pasteName =
      paste.getPasteName() == null || paste.getPasteName().isEmpty() ? "N/D" : paste.getPasteName();
    setTitle(pasteName);

    textView = (TextView) findViewById(R.id.codeview);
    downloaded = (savedInstanceState != null) && savedInstanceState.getBoolean("downloaded");
    manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

      if (!downloaded) {
          new DownloadRAW().execute();
      }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putBoolean("downloaded", downloaded);
    super.onSaveInstanceState(outState);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    Intent intent;
    switch (item.getItemId()) {
      case R.id.forkpaste:
        intent = new Intent(this, ShareCodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ShareCodeActivity.EXTRA_FLAG_FORK, textView.getText().toString());
        startActivity(intent);
        break;
      case R.id.openinbrowser:
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://pastebin.com/" + pasteKey));
        startActivity(intent);
        break;
      case R.id.downloadpaste:
        String text = textView.getText().toString();

        if (text.length() == 0) {
          Toast.makeText(this, R.string.emptypaste, Toast.LENGTH_SHORT).show();
          break;
        }

        File
          file =
          new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                   "Paste_" + pasteKey + ".txt");

        if (file.exists() && !downloadConfirm) {
          Toast.makeText(this, R.string.confirmdownload, Toast.LENGTH_SHORT).show();
          downloadConfirm = true;
          break;
        }

        downloadConfirm = false;
        OutputStream outputStream = null;
        try {
          outputStream = new FileOutputStream(file);
          outputStream.write(text.getBytes());

          Toast.makeText(this, R.string.downloadok, Toast.LENGTH_SHORT).show();
          downloadOKNotification(getString(R.string.localdownlodok, paste.getPasteName(), pasteKey),
                                 file);
        } catch (IOException e) {
          Toast.makeText(this, R.string.downloadfail, Toast.LENGTH_LONG).show();
          Log.d(ShareCodeActivity.DEBUG_TAG, Log.getStackTraceString(e));
        } finally {
          if (outputStream != null) {
            try {
              outputStream.close();
            } catch (IOException e) {
              Log.d(ShareCodeActivity.DEBUG_TAG, "Exception in ExplorePaste", e);
            }
          }
        }
        break;
    }
    return super.onMenuItemSelected(featureId,
                                    item);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.explorepastemenu, menu);
//        MenuItem item = menu.findItem(R.id.sharepaste);
//        shareActionProvider = (ShareActionProvider) item.getActionProvider();

    return super.onCreateOptionsMenu(
      menu);    //To change body of overridden methods use File | Settings | File Templates.
  }

  private void downloadOKNotification(String content, File file) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.fromFile(file), "text/*");

    PendingIntent pendingIntent = PendingIntent.getActivity(
      this,
      0,
      intent,
      0
    );

    Notification notification = CompatibleNotification.createNotification(this)
      .setSmallIcon(R.drawable.ic_action_download)
      .setContentTitle(getString(R.string.app_name))
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .setContentText(content)
      .create();

    final int notificationDownloadID = 1;
    manager.notify(notificationDownloadID, notification);
  }

  class DownloadRAW extends AsyncTask<Void, Void, String> {

    private ProgressDialog alertDialog;

    @Override
    protected String doInBackground(Void... params) {
      String finalResponse = null;

      try {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response;
        HttpGet httpGet = new HttpGet("http://pastebin.com/raw.php?i=" + pasteKey);

        response = client.execute(httpGet);
        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          response.getEntity().writeTo(outputStream);
          outputStream.close();

          finalResponse = outputStream.toString();
        } else {
          response.getEntity().getContent().close();
        }
      } catch (IOException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }

      return finalResponse;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onPreExecute() {
      super
        .onPreExecute();    //To change body of overridden methods use File | Settings | File Templates.

      alertDialog = new ProgressDialog(ExplorePaste.this);
      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                finish();
                              }
                            });
      alertDialog.setMessage(ExplorePaste.this.getString(R.string.waitdownload));
      alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
          if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
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
    protected void onPostExecute(String s) {
      super.onPostExecute(
        s);    //To change body of overridden methods use File | Settings | File Templates.

      downloaded = true;
      if (s != null) {
        textView.setText(s);
      }
      //no paste here

        if (!ExplorePaste.this.isFinishing()) {
            alertDialog.dismiss();
        }
    }
  }
}