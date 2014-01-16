package com.revonline.pastebin.xml;

import android.util.Log;
import com.revonline.pastebin.MyActivity;
import com.revonline.pastebin.PasteInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 01/12/13
 * Time: 16.45
 * To change this template use File | Settings | File Templates.
 */
public class XMLHandler extends DefaultHandler {
    private static final String XML_ROOT_ELEMENT = "paste";
    private static final String XML_PASTE_KEY = "paste_key";
    private static final String XML_PASTE_DATE = "paste_date";
    private static final String XML_PASTE_TITLE = "paste_title";
    private static final String XML_PASTE_LANGUAGE = "paste_format_long";
    private boolean onElement;
    private String value;
    public ArrayList<PasteInfo> data = new ArrayList<PasteInfo>();
    private PasteInfo info = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        onElement = true;
        Log.d(MyActivity.DEBUG_TAG, "uri => " + uri);
        Log.d(MyActivity.DEBUG_TAG, "localName => " + localName);

        if (localName.equals(XML_ROOT_ELEMENT))
        {
            Log.d(MyActivity.DEBUG_TAG, "root element");

            if (info != null)
            {
                Log.d(MyActivity.DEBUG_TAG, "add new item");
                data.add(info);
                info = null;
            }

            Log.d(MyActivity.DEBUG_TAG, "creo..");
            info = new PasteInfo();
        }
    }

    /*
    function timeToDate(f){
      if(!f.inPut.value.length){
        f.output.value = "";
        return;
      }
      f.output.value = new Date(parseInt(f.inPut.value)*(f.milli.checked?1:1000));
      //pageTracker._trackPageview("/dateCalcs/millisecond-to-date-string");
      pageTracker._trackEvent("dateCalculators", "millisecond-to-date-string", f.output.value);
    }

     */

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        onElement = false;

        if (localName.equals(XML_PASTE_KEY))
        {
            info.setPasteKey(value);
            Log.d(MyActivity.DEBUG_TAG, "XML PARSER -- paste key " + value);
        }
        else if (localName.equals(XML_PASTE_DATE))
        {
            info.setPasteData(Calendar.getInstance());
            // SEMBRA ANDARE IN OVERFLOW MA NON DOVREBBE
            long time = Long.parseLong(value) * 1000;
            info.getPasteData().setTimeInMillis(time);

            Log.d(MyActivity.DEBUG_TAG, "XML PARSER -- paste date -- temp removed... " + time);
        }
        else if (localName.equals(XML_PASTE_TITLE))
        {
            info.setPasteName(value);
            Log.d(MyActivity.DEBUG_TAG, "XML PARSER -- paste name " + value);
        }
        else if (localName.equals(XML_PASTE_LANGUAGE))
        {
            info.setPasteLanguage(value);
            Log.d(MyActivity.DEBUG_TAG, "XML PARSER -- paste language " + value);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (onElement)
        {
            value = new String(ch, start, length);
            onElement = false;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (info != null)
        {
            data.add(info);
            value = "Untitled";
        }
    }

    /*
     <paste>
     <paste_key>4eWYATXe</paste_key>
     <paste_date>1319458935</paste_date>
     <paste_title>577 French MPs</paste_title>
     <paste_size>29397</paste_size>
     <paste_expire_date>0</paste_expire_date>
     <paste_private>0</paste_private>
     <paste_format_long>None</paste_format_long>
     <paste_format_short>text</paste_format_short>
     <paste_url>http://pastebin.com/4eWYATXe</paste_url>
     <paste_hits>804</paste_hits>
     </paste>
     */
}
