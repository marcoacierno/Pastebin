package com.revonline.pastebin.trending_pastes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TimingLogger;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.revonline.pastebin.ErrorMessages;
import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.R;
import com.revonline.pastebin.ShareCodeActivity;
import com.revonline.pastebin.SpecialKeys;
import com.revonline.pastebin.adapters.PastesListAdapter;
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
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 01/12/13 Time: 12.01 To change this template use
 * File | Settings | File Templates.
 */
public class PopPastes extends Activity {

  public static final String KEY_POP_PASTES = "poppastes";
  public static final String CACHE_PASTES = "pastes";
  DialogInterface.OnClickListener retry = new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
      new DownloadTrendingPastes().execute();
    }
  };
  DialogInterface.OnClickListener close = new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
      finish();
    }
  };
  private PastesListAdapter adapter;
  private ArrayList<PasteInfo> pasteInfos;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.trendingpastes);

    adapter = new PastesListAdapter(this);

    if (savedInstanceState != null) {
      pasteInfos = savedInstanceState.getParcelableArrayList(KEY_POP_PASTES);
    } else {
      //
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//            String cached_xml = sharedPreferences.getString("cachexml", null);

      long lastDownload = sharedPreferences.getLong("lastdownload", 0);

      Log.d(ShareCodeActivity.DEBUG_TAG, "lastDownload=>" + lastDownload);

      File file = new File(getCacheDir(), CACHE_PASTES);
      if (file.exists()) {
        int hours = Hours.hoursBetween(
          new DateTime(lastDownload),
          DateTime.now()
        ).getHours();

        Log.d(ShareCodeActivity.DEBUG_TAG, "diff: " + hours);

        // non è passata un'ora, quindi uso la cache
        if (hours == 0) {
          ObjectInputStream objectInputStream = null;
          pasteInfos = new ArrayList<>();

          TimingLogger
            logger =
            new TimingLogger(ShareCodeActivity.DEBUG_TAG, "recent pastes restore");

          try {
            objectInputStream = new ObjectInputStream(new FileInputStream(file));

            while (true) {
              try {
                pasteInfos.add((PasteInfo) objectInputStream.readObject());
              } catch (ClassNotFoundException e) {
                break;
              } catch (EOFException e) {
                break;
              }
            }

            logger.addSplit("restore");
            logger.dumpToLog();

//                        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
//                        SAXParser parser = saxParserFactory.newSAXParser();
//                        XMLReader reader = parser.getXMLReader();
//                        XMLHandler handler = new XMLHandler();
//                        reader.setContentHandler(handler);
//                        reader.parse(new InputSource(new StringReader("<root>" + cached_xml + "</root>")));
//
//                        pasteInfos = handler.data;
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            if (objectInputStream != null) {
              try {
                objectInputStream.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        } else {
          file.delete();// passata un'ora, cancello il file
        }
      }
    }

    if (!ShareCodeActivity.apiLower11) {
      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    setTitle(R.string.pastepopolari);

    ListView listView = (ListView) findViewById(R.id.treadingpastes);
    listView.setAdapter(adapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(parent.getContext(), ExplorePaste.class);
        //Log.d(ShareCodeActivity.DEBUG_TAG, "parent.getItemIdAtPosition(position) => " + parent.getItemIdAtPosition(position));
        intent
          .putExtra(ExplorePaste.EXTRA_PASTE_INFO, (Parcelable) parent.getItemAtPosition(position));
        startActivity(intent);
      }
    });

    listView.setEmptyView(findViewById(R.id.empty));

    if (pasteInfos == null || pasteInfos.size() == 0) {
      new DownloadTrendingPastes().execute();
    } else {
      adapter.setPasteInfoList(pasteInfos);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putParcelableArrayList(KEY_POP_PASTES, pasteInfos);
//        Log.d(ShareCodeActivity.DEBUG_TAG, "onSaveInstanceState->pasteInfos => " + pasteInfos);
//        Log.d(ShareCodeActivity.DEBUG_TAG, "outState " + outState.getParcelableArrayList("poppastes"));

    super.onSaveInstanceState(outState);
  }

  //<Params, Progress, Result>
  class DownloadTrendingPastes extends AsyncTask<Void, Void, String> {

    ProgressDialog alertDialog;

    @Override
    protected String doInBackground(Void... params) {
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost("http://pastebin.com/api/api_post.php");
      HttpResponse response;
      String bodyresponse = null;
      List<BasicNameValuePair> pairs = new ArrayList<>();

      pairs.add(new BasicNameValuePair("api_option", "trends"));
      pairs.add(new BasicNameValuePair("api_dev_key", SpecialKeys.DEV_KEY));

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
          XMLHandler handler = new XMLHandler(PopPastes.this);
          reader.setContentHandler(handler);
          reader.parse(new InputSource(new StringReader("<root>" + bodyresponse + "</root>")));

          pasteInfos = handler.data;
        } else {
          response.getEntity().getContent().close();
        }
      } catch (IOException | SAXException | ParserConfigurationException e) {
        Log.d(ShareCodeActivity.DEBUG_TAG, "Exception: ", e);
      }

      return bodyresponse;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onPreExecute() {
      super
        .onPreExecute();    //To change body of overridden methods use File | Settings | File Templates.

      alertDialog = new ProgressDialog(PopPastes.this);
      alertDialog.setMessage(PopPastes.this.getString(R.string.waitdownloadlist));
      alertDialog.setCancelable(false);
      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                finish();
                              }
                            });
      alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialogInterface, int keyCode,
                             KeyEvent keyEvent) {
          if (keyCode == KeyEvent.KEYCODE_BACK
              && keyEvent.getAction() == KeyEvent.ACTION_UP) {
            finish();
            return true;
          }

          return false;
        }
      });
      alertDialog.show();
    }

    @Override
    protected void onPostExecute(String xml) {
      super.onPostExecute(
        xml);    //To change body of overridden methods use File | Settings | File Templates.
      if (PopPastes.this.isFinishing()) {
        return;
      }
      if (pasteInfos == null) {
        return;
      }

      SharedPreferences.Editor
        editor =
        PreferenceManager.getDefaultSharedPreferences(PopPastes.this).edit();
//            editor.putString("cachexml", xml);
      editor.putLong("lastdownload", new DateTime().getMillis());
      editor.commit();

      // cache it
      File file = new File(getCacheDir(), CACHE_PASTES);
      ObjectOutputStream outputStream = null;

      try {
        outputStream = new ObjectOutputStream(new FileOutputStream(file));

        for (PasteInfo pasteInfo : pasteInfos) {
          outputStream.writeObject(pasteInfo);
        }

        outputStream.flush();
      } catch (IOException ignored) {
      } finally {
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

//            Log.d(ShareCodeActivity.DEBUG_TAG, "pasteInfos = " + pasteInfos);

      alertDialog.dismiss();
      if (pasteInfos != null && pasteInfos.size() > 0) {
        adapter.setPasteInfoList(pasteInfos);
      } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(PopPastes.this);

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
}