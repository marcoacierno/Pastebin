package com.revonline.pastebin.trending_pastes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.ShareCodeActivity;
import com.revonline.pastebin.xml.XMLHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 *
 */
public class DeserializePastesFromCacheTask extends AsyncTask<String, Void, List<PasteInfo>> {
  private final WeakReference<Context> contextWeakReference;
  private final WeakReference<DeserializePastesListener> weakListener;

  public DeserializePastesFromCacheTask(final Context context, final DeserializePastesListener listener) {
    this.contextWeakReference = new WeakReference<>(context);
    this.weakListener = new WeakReference<>(listener);
  }

  @Override
  protected List<PasteInfo> doInBackground(final String... params) {
    final Context context = contextWeakReference.get();

    if (context == null) {
      return null;
    }

    final String cached_xml = params[0];
    try {
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      SAXParser parser = saxParserFactory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      XMLHandler handler = new XMLHandler(context);

      reader.setContentHandler(handler);
      reader.parse(new InputSource(new StringReader("<root>" + cached_xml + "</root>")));

      return handler.data;
    } catch (IOException | ParserConfigurationException | SAXException e) {
      Log.e(ShareCodeActivity.DEBUG_TAG, "mentre caricavo i trending pastes", e);
    }

    return null;
  }

  @Override
  protected void onPostExecute(final List<PasteInfo> pasteInfos) {
    super.onPostExecute(pasteInfos);

    final DeserializePastesListener listener = this.weakListener.get();

    if (listener != null) {
      listener.onDeserializePastesResult(pasteInfos);
    }
  }
}
