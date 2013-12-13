package com.revonline.pastebin;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.revonline.pastebin.database.PasteDBHelper;
import com.revonline.pastebin.explorepaste.ExplorePaste;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 30/11/13
 * Time: 19.41
 * To change this template use File | Settings | File Templates.
 */

//immaginarti con altri
//la follia definitiva.

public class CodeShareReceiver extends BroadcastReceiver {
    public final static String SHARE_SUCCESS = "pastebin.SHARE_SUCCESS";

    public void onReceive(final Context context, Intent intent) {
        Log.d(MyActivity.DEBUG_TAG, "CodeShareReceiver - onReceive");
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
            Log.d(MyActivity.DEBUG_TAG, "errors.get(finalResponse) => " + ErrorMessages.errors.get(finalResponse));
            String response = context.getString(ErrorMessages.errors.get(finalResponse));

            alertDialog.setTitle(R.string.errore);
            alertDialog.setMessage(context.getString(R.string.msgerrore, response));
            alertDialog.setNegativeButton(R.string.close, null);
        }
        else
        {
            alertDialog.setTitle(R.string.result);
            alertDialog.setMessage(context.getString(R.string.uploadsuccess, finalResponse));
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

            PasteDBHelper pasteDBHelper = new PasteDBHelper(context);

            //(String name, String language, String scadenza, int tipo, String url)
            pasteDBHelper.addPaste(
                    intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_NAME),
                    intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_LANG),
                    intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_SCADENZA),
                    intent.getIntExtra(Pastebin.EXTRA_FLAG_PASTE_PRIVATE, 0),
                    key);
        }
        alertDialog.show();
    }
}
