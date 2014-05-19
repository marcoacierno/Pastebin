package com.revonline.pastebin.collections.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 30/11/13
 * Time: 21.57
 * To change this template use File | Settings | File Templates.
 */
public class ArgsPair implements Parcelable {
    List<ParcelableNameValuePair> pairs = new ArrayList<ParcelableNameValuePair>();

    public void add(ParcelableNameValuePair basicNameValuePair)
    {
        pairs.add(basicNameValuePair);
    }

    public ArgsPair() { }

    public ArgsPair(Parcel in)
    {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeTypedList(pairs);
    }

    @SuppressWarnings("unchecked")
    public void readFromParcel(Parcel in)
    {
        in.readTypedList(pairs, ParcelableNameValuePair.CREATOR);
    }

    public List<ParcelableNameValuePair> getList() { return pairs; }

    public static final Creator CREATOR = new Creator() {
        public ArgsPair createFromParcel(Parcel in) {
            return new ArgsPair(in);
        }

        public ArgsPair[] newArray(int size) {
            return new ArgsPair[size];
        }
    };
}
