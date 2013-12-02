package com.revonline.pastebin;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.revonline.pastebin.collections.parcelable.ArgsPair;
import com.revonline.pastebin.collections.parcelable.ParcelableNameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 30/11/13
 * Time: 19.23
 * To change this template use File | Settings | File Templates.
 */
public class SendCode extends IntentService {
    public static final String FLAG_EXTRA_HTTP_RESULT = "SendCode.HTTP_RESULT";
    public SendCode() {
        super("sendcode");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SendCode(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(MyActivity.DEBUG_TAG, "onHandleIntent");

        ArgsPair args = intent.getParcelableExtra(Pastebin.EXTRA_FLAG_PASTE_ARGS);

        if (args == null)
        {
            return;
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://pastebin.com/api/api_post.php");
        HttpResponse response;
        String finalResponse = null;
        List<ParcelableNameValuePair> list = args.getList();

        try
        {
            post.setEntity(new UrlEncodedFormEntity(list));

            response = httpClient.execute(post);
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
            // ToDo: Something
            e.printStackTrace();
        } catch (IOException e) {
            // ToDo: Something
            e.printStackTrace();
        }

        Intent broadcast = new Intent();
        broadcast.setAction(codeshareReceiver.SHARE_SUCCESS);
        broadcast.addCategory(Intent.CATEGORY_DEFAULT);
        broadcast.putExtra(FLAG_EXTRA_HTTP_RESULT, finalResponse);

        broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_NAME, intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_NAME));
        broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_LANG, intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_LANG));
        broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_PRIVATE, intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_PRIVATE));
        broadcast.putExtra(Pastebin.EXTRA_FLAG_PASTE_SCADENZA, intent.getStringExtra(Pastebin.EXTRA_FLAG_PASTE_SCADENZA));

        sendBroadcast(broadcast);

        Log.d(MyActivity.DEBUG_TAG, "SendCode - fine");
    }
}
