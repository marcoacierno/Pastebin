package com.revonline.pastebin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.revonline.pastebin.collections.parcelable.ArgsPair;
import com.revonline.pastebin.collections.parcelable.ParcelableNameValuePair;
import com.revonline.pastebin.service.SendCodeService;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 30/11/13 Time: 17.45 To change this template use
 * File | Settings | File Templates.
 */
public class Pastebin {

  public static final String EXTRA_FLAG_PASTE_NAME = "pastebin.EXTRA_FLAG.NAME";
  public static final String EXTRA_FLAG_PASTE_LANG = "pastebin.EXTRA_FLAG.LANG";
  public static final String EXTRA_FLAG_PASTE_PRIVATE = "pastebin.EXTRA_FLAG.PRIVATE";
  public static final String EXTRA_FLAG_PASTE_SCADENZA = "pastebin.EXTRA_FLAG.SCADENZA";
  public static final String EXTRA_FLAG_PASTE_ARGS = "pastebin.EXTRA_FLAG.ARGS";
  private Context context;

  public Pastebin(Context context) {
    this.context = context;
  }

  public void postPaste(String title, String code, String language, String scadenza, int visibility,
                        boolean anonimo, String key) {
    if (title == null) {
      // it's not localized because it's not localized by pastebin
      // Untitled is the default name of a no-title
      title = "Untitled";
    }

    // anonimo => true se il paste deve essere anonimo
    // false => se il paste deve essere dell'utente (se loggato)

    Log.d(ShareCodeActivity.DEBUG_TAG, "visibility => " + visibility);

    if (code.length() < 1) {
      return;
    }

    String visibilita = String.valueOf(visibility);

    ArgsPair argsPair = new ArgsPair();
    argsPair.add(new ParcelableNameValuePair("api_option", "paste"));
    argsPair.add(new ParcelableNameValuePair("api_paste_private", visibilita));
    argsPair.add(new ParcelableNameValuePair("api_paste_name", title));
    argsPair.add(new ParcelableNameValuePair("api_paste_expire_date", scadenza));
    argsPair.add(new ParcelableNameValuePair("api_paste_format", language));
    argsPair.add(new ParcelableNameValuePair("api_dev_key", SpecialKeys.DEV_KEY));
    argsPair.add(new ParcelableNameValuePair("api_paste_code", code));
    if (!anonimo) {
      argsPair.add(new ParcelableNameValuePair("api_user_key", key));
    }
    //http://pastebin.com/api/api_post.php

    Intent intent = new Intent(context, SendCodeService.class);
    intent.putExtra(EXTRA_FLAG_PASTE_ARGS, argsPair);
    intent.putExtra(EXTRA_FLAG_PASTE_NAME, title);
    intent.putExtra(EXTRA_FLAG_PASTE_LANG, language);
    intent.putExtra(EXTRA_FLAG_PASTE_PRIVATE, visibilita);
    intent.putExtra(EXTRA_FLAG_PASTE_SCADENZA, scadenza);

    Log.d(ShareCodeActivity.DEBUG_TAG, "launch service");
    context.startService(intent);

    Toast.makeText(context, R.string.sharestarted, Toast.LENGTH_LONG).show();
  }
}
