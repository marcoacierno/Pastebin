package com.revonline.pastebin;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import com.revonline.pastebin.database.PasteDBHelper;
import com.revonline.pastebin.explorepaste.ExplorePaste;
import org.apache.commons.validator.routines.UrlValidator;
import org.joda.time.DateTime;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 30/11/13
 * Time: 19.41
 * To change this template use File | Settings | File Templates.
 */

//immaginarti con altri
//la follia definitiva.

public class CodeShareReceiver extends BroadcastReceiver
{
    public final static String SHARE_SUCCESS = "pastebin.SHARE_SUCCESS";

    public void onReceive(final Context context, final Intent intent) {
        Log.d(ShareCodeActivity.DEBUG_TAG, "CodeShareReceiver - onReceive");
        final String finalResponse = intent.getStringExtra(SendCodeService.FLAG_EXTRA_HTTP_RESULT);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        if (finalResponse == null)
        {
            alertDialog.setTitle(R.string.errore);
            alertDialog.setMessage(R.string.nointernet);
            alertDialog.setPositiveButton(R.string.OK, null);
            alertDialog.show();
            return;
        }

        Log.d(ShareCodeActivity.DEBUG_TAG, "finalResponse = " + finalResponse);

        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(finalResponse))
        {
            Log.d(ShareCodeActivity.DEBUG_TAG, "finalResponse => " + finalResponse);
            Log.d(ShareCodeActivity.DEBUG_TAG, "errors.get(finalResponse) => " + ErrorMessages.errors.get(finalResponse));
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

            alertDialog.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //To change body of implemented methods use File | Settings | File Templates.
                    Intent explore = new Intent(context, ExplorePaste.class);
                    PasteInfo pasteInfo = new PasteInfo(
                            intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_NAME),
                            null,
                            intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_LANG),
                            null,
                            key
                    );
                    pasteInfo.setPasteData(Calendar.getInstance());
                    pasteInfo.getPasteData().setTimeInMillis(DateTime.now().toInstant().getMillis());

                    explore.putExtra(ExplorePaste.EXTRA_PASTE_INFO, (Parcelable) pasteInfo);
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
