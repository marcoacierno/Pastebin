package com.revonline.pastebin.collections.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.NameValuePair;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 30/11/13 Time: 21.57 To change this template use
 * File | Settings | File Templates.
 */
public class ParcelableNameValuePair implements Parcelable, NameValuePair {

  public static final Creator CREATOR = new Creator() {
    public ParcelableNameValuePair createFromParcel(Parcel in) {
      return new ParcelableNameValuePair(in);
    }

    public ParcelableNameValuePair[] newArray(int size) {
      return new ParcelableNameValuePair[size];
    }
  };
  public String name;
  public String value;

  public ParcelableNameValuePair(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public ParcelableNameValuePair(Parcel in) {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in) {
    name = in.readString();
    value = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(value);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return value;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
