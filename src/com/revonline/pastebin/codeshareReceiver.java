package com.revonline.pastebin;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import com.revonline.pastebin.database.SQLiteHelper;
import com.revonline.pastebin.explorepaste.ExplorePaste;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 30/11/13
 * Time: 19.41
 * To change this template use File | Settings | File Templates.
 */

//immaginarti con altri
//la follia definitiva.

public class codeshareReceiver extends BroadcastReceiver {
    public final static String SHARE_SUCCESS = "pastebin.SHARE_SUCCESS";
    public final static HashMap<String, Integer> errors = new HashMap<String, Integer>();

    static
    {
        errors.put("Bad API request, invalid api_option", R.string.erroreinterno);
        errors.put("Bad API request, invalid api_dev_key", R.string.erroreinterno);
        errors.put("Bad API request, IP blocked", R.string.blockedip);
        errors.put("Bad API request, maximum number of 25 unlisted pastes for your free account", R.string.maxunlimitedpastes);
        errors.put("Bad API request, maximum number of 10 private pastes for your free account", R.string.maxprivatepastes);
        errors.put("Bad API request, api_paste_code was empty", R.string.blankcode);
        errors.put("Bad API request, maximum paste file size exceeded", R.string.maxpastesize);
        errors.put("Bad API request, invalid api_expire_date", R.string.erroreinterno);
        errors.put("Bad API request, invalid api_paste_private", R.string.erroreinterno);
        errors.put("Bad API request, invalid api_paste_format", R.string.erroreinterno);
        errors.put("Post limit, maximum pastes per 24h reached", R.string.maxpastes);
    }

    public void onReceive(final Context context, Intent intent) {
        Log.d(MyActivity.DEBUG_TAG, "codeshareReceiver - onReceive");
        final String finalResponse = intent.getStringExtra(SendCode.FLAG_EXTRA_HTTP_RESULT);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        if (finalResponse == null)
        {
            alertDialog.setTitle(R.string.errore);
            alertDialog.setMessage(R.string.nointernet);
            alertDialog.setPositiveButton(R.string.OK, null);
            alertDialog.show();
            return;
        }

        Log.d(MyActivity.DEBUG_TAG, "finalResponse = " + finalResponse);

        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(finalResponse))
        {
            Log.d(MyActivity.DEBUG_TAG, "finalResponse => " + finalResponse);
            Log.d(MyActivity.DEBUG_TAG, "errors.get(finalResponse) => " + errors.get(finalResponse));
            String response = context.getString(errors.get(finalResponse));

            alertDialog.setTitle(R.string.errore);
            alertDialog.setMessage("Si è verificato un errore: " + response + ". Riprova.");
            alertDialog.setNegativeButton(R.string.close, null);
        }
        else
        {
            alertDialog.setTitle(R.string.result);
            alertDialog.setMessage("Il paste è stato pubblicato con successo! URL: " + finalResponse);
            final String key = finalResponse.substring(finalResponse.lastIndexOf('/') + 1);

            AlertDialog.Builder builder = alertDialog.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //To change body of implemented methods use File | Settings | File Templates.
                    Intent explore = new Intent(context, ExplorePaste.class);
                    explore.putExtra(ExplorePaste.FLAG_EXTRA_PASTE_URL, key);
                    context.startActivity(explore);
                }
            });

            alertDialog.setNegativeButton(R.string.close, null);

            SQLiteHelper sqLiteHelper = new SQLiteHelper(context);

            //(String name, String language, String scadenza, int tipo, String url)
            sqLiteHelper.addPaste(
                    intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_NAME),
                    intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_LANG),
                    intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_SCADENZA),
                    intent.getIntExtra(Pastebin.EXTRA_FLAG_PASTE_PRIVATE, 0),
                    key);
        }
        alertDialog.show();
    }
}
