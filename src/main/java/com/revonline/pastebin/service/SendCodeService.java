package com.revonline.pastebin.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.revonline.pastebin.CodeShareReceiver;
import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.Pastebin;
import com.revonline.pastebin.R;
import com.revonline.pastebin.ShareCodeActivity;
import com.revonline.pastebin.collections.parcelable.ArgsPair;
import com.revonline.pastebin.collections.parcelable.ParcelableNameValuePair;

import com.revonline.pastebin.explorepaste.ExplorePaste;
import com.revonline.pastebin.notification.CompatibleNotification;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 30/11/13 Time: 19.23 To change this template use
 * File | Settings | File Templates.
 */
public class SendCodeService extends IntentService {

  public static final String FLAG_EXTRA_HTTP_RESULT = "SendCodeService.HTTP_RESULT";

  public SendCodeService() {
    super("sendcode");
  }

  /**
   * Creates an IntentService.  Invoked by your subclass's constructor.
   *
   * @param name Used to name the worker thread, important only for debugging.
   */
  public SendCodeService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(ShareCodeActivity.DEBUG_TAG, "onHandleIntent");
    ArgsPair args = intent.getParcelableExtra(Pastebin.EXTRA_FLAG_PASTE_ARGS);

    if (args == null) {
      return;
    }

    HttpClient httpClient = new DefaultHttpClient();
    HttpPost post = new HttpPost("http://pastebin.com/api/api_post.php");
    HttpResponse response;
    String finalResponse = null;
    List<ParcelableNameValuePair> list = args.getList();

    try {
      post.setEntity(new UrlEncodedFormEntity(list));

      response = httpClient.execute(post);
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
      // ToDo: Something
      e.printStackTrace();
    }

    Intent broadcast = new Intent();
    broadcast.setAction(CodeShareReceiver.SHARE_SUCCESS);
    broadcast.addCategory(Intent.CATEGORY_DEFAULT);
    broadcast.putExtra(FLAG_EXTRA_HTTP_RESULT, finalResponse);

    final String name = intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_NAME);

    broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_NAME, name);
    broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_LANG,
                       intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_LANG));
    broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_PRIVATE,
                       intent.getIntExtra(Pastebin.EXTRA_FLAG_PASTE_PRIVATE, 0));
    broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_SCADENZA,
                       intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_SCADENZA));

    sendBroadcast(broadcast);

    Log.d(ShareCodeActivity.DEBUG_TAG, "SendCodeService - fine");
  }
}
