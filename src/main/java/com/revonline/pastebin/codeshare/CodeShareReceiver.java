package com.revonline.pastebin.codeshare;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.revonline.pastebin.ErrorMessages;
import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.Pastebin;
import com.revonline.pastebin.R;
import com.revonline.pastebin.ShareCodeActivity;
import com.revonline.pastebin.database.PasteDBHelper;
import com.revonline.pastebin.explorepaste.ExplorePaste;
import com.revonline.pastebin.notification.CompatibleNotification;
import com.revonline.pastebin.codeshare.SendCodeService;

import org.apache.commons.validator.routines.UrlValidator;
import org.joda.time.DateTime;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 30/11/13 Time: 19.41 To change this template use
 * File | Settings | File Templates.
 */

//immaginarti con altri
//la follia definitiva.

public class CodeShareReceiver extends BroadcastReceiver {

  public final static String SHARE_SUCCESS = "pastebin.SHARE_SUCCESS";

  public void onReceive(final Context context, final Intent intent) {
    Log.d(ShareCodeActivity.DEBUG_TAG, "CodeShareReceiver - onReceive");
    final String finalResponse = intent.getStringExtra(SendCodeService.FLAG_EXTRA_HTTP_RESULT);

    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
    if (finalResponse == null) {
      alertDialog.setTitle(R.string.errore);
      alertDialog.setMessage(R.string.nointernet);
      alertDialog.setPositiveButton(R.string.OK, null);
      alertDialog.show();
      return;
    }

    Log.d(ShareCodeActivity.DEBUG_TAG, "finalResponse = " + finalResponse);

    UrlValidator urlValidator = new UrlValidator();
    if (!urlValidator.isValid(finalResponse)) {
      Log.d(ShareCodeActivity.DEBUG_TAG, "finalResponse => " + finalResponse);
      Log.d(ShareCodeActivity.DEBUG_TAG,
            "errors.get(finalResponse) => " + ErrorMessages.getErrorFor(finalResponse));
      String response = context.getString(ErrorMessages.getErrorFor(finalResponse));

      alertDialog.setTitle(R.string.errore);
      alertDialog.setMessage(context.getString(R.string.msgerrore, response));
      alertDialog.setNegativeButton(R.string.close, null);
    } else {
      final Intent explore = new Intent(context, ExplorePaste.class);

      final String key = finalResponse.substring(finalResponse.lastIndexOf('/') + 1);
      final String name = intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_NAME);

      PasteInfo pasteInfo = new PasteInfo(
          name,
          null,
          intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_LANG),
          null,
          key
      );

      pasteInfo.setPasteData(Calendar.getInstance());
      pasteInfo.getPasteData().setTimeInMillis(DateTime.now().toInstant().getMillis());

      explore.putExtra(ExplorePaste.EXTRA_PASTE_INFO, (Parcelable) pasteInfo);

      alertDialog.setTitle(R.string.result);
      alertDialog.setMessage(context.getString(R.string.uploadsuccess, finalResponse));
      alertDialog.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          //To change body of implemented methods use File | Settings | File Templates.
          context.startActivity(explore);
        }
      });

      alertDialog.setNegativeButton(R.string.close, null);

      alertDialog.setNeutralButton(context.getString(R.string.copyUrl), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
          if (!ShareCodeActivity.apiLower11) {
            final android.content.ClipboardManager clipboardManager = ((android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE));
            final android.content.ClipData.Item item = new android.content.ClipData.Item(finalResponse);
            final android.content.ClipData clipData = new android.content.ClipData("Pastebin Url", new String[]{"text/text"}, item);
            clipboardManager.setPrimaryClip(clipData);
          } else {
            final android.text.ClipboardManager clipboardManager
                = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(finalResponse);
          }
        }
      });

      PasteDBHelper pasteDBHelper = new PasteDBHelper(context);

      //(String name, String language, String scadenza, int tipo, String url)
      pasteDBHelper.addPaste(
          name,
        intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_LANG),
        intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_SCADENZA),
        intent.getIntExtra(Pastebin.EXTRA_FLAG_PASTE_PRIVATE, 0),
        key);

      final PendingIntent pendingIntent = PendingIntent.getActivity(
          context,
          3,
          explore,
          PendingIntent.FLAG_UPDATE_CURRENT
      );

      final Notification notification = CompatibleNotification.createNotification(context)
        .setContentTitle(context.getString(R.string.pasteshared, name))
        .setContentText(context.getString(R.string.clicktosee))
        .setSmallIcon(R.drawable.ic_action_share)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .create();

      final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      manager.notify(3, notification);
    }
    alertDialog.show();
  }
}
