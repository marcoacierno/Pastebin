package com.revonline.pastebin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.revonline.pastebin.PasteInfo;
import com.revonline.pastebin.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 01/12/13 Time: 15.46 To change this template use
 * File | Settings | File Templates.
 */
public class PastesListAdapter extends BaseAdapter {

  private Context context;
  private List<PasteInfo> pasteInfoList = new ArrayList<>();
  private LayoutInflater inflater;
  private final static DateFormat format = new SimpleDateFormat("MMMM E dd, yyyy HH:MM");

  public PastesListAdapter(Context context) {
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public PastesListAdapter(Context context, List<PasteInfo> pasteInfoList) {
    this.context = context;
    this.pasteInfoList = pasteInfoList;
  }

//    public PastesListAdapter(Context context, List<PasteInfo> pasteInfoList, TreeSet separators) {
//        this.context = context;
//        this.pasteInfoList = pasteInfoList;
//        this.separators = separators;
//    }


  public void setPasteInfoList(List<PasteInfo> pasteInfoList) {
    this.pasteInfoList = pasteInfoList;
    notifyDataSetChanged();
  }


  @Override
  public int getCount() {
    return pasteInfoList
      .size();  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Object getItem(int position) {
    return pasteInfoList
      .get(position);  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public long getItemId(int position) {
    return position;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;

    if (convertView == null) {
      convertView = inflater.inflate(R.layout.pasterow, parent, false);

      viewHolder = new ViewHolder();

      viewHolder.name = (TextView) convertView.findViewById(R.id.namepaste);
      viewHolder.subText = (TextView) convertView.findViewById(R.id.submessage);
      viewHolder.time = (TextView) convertView.findViewById(R.id.pastetime);

      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    PasteInfo pasteInfo = (PasteInfo) getItem(position);
    viewHolder.name.setText(pasteInfo.getPasteName());
    String author = pasteInfo.getPasteAuthor();
    viewHolder.subText.setText(
      (author == null ? context.getString(R.string.noauthor) : author) + " - " + pasteInfo
        .getPasteLanguage());
    Calendar date = pasteInfo.getPasteData();
    viewHolder.time.setText(format.format(date.getTime()));
//        viewHolder.time.setText(String.format("%02d/%02d/%d", date.get(Calendar.DAY_OF_MONTH), (date.get(Calendar.MONTH) + 1), date.get(Calendar.YEAR)));
//        Log.d(ShareCodeActivity.DEBUG_TAG, "date == " + date);

    return convertView;  //To change body of implemented methods use File | Settings | File Templates.
  }

  private static class ViewHolder {

    public TextView name;
    public TextView subText;
    public TextView time;
  }
}
