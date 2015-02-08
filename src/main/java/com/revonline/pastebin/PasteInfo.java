package com.revonline.pastebin;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;

public class PasteInfo implements Parcelable, Serializable {

  public static final Parcelable.Creator<PasteInfo> CREATOR
    = new Parcelable.Creator<PasteInfo>() {
    public PasteInfo createFromParcel(Parcel in) {
      return new PasteInfo(in);
    }

    public PasteInfo[] newArray(int size) {
      return new PasteInfo[size];
    }
  };
  private int sqlID;
  private String pasteName;
  private String pasteAuthor;
  private String pasteLanguage = "Unknown";
  private Calendar pasteData;
  private String pasteKey;

  public PasteInfo(Parcel in) {
    readFromParcel(in);
  }

  private void readFromParcel(Parcel in) {
    sqlID = in.readInt();
    pasteName = in.readString();
    pasteAuthor = in.readString();
    pasteLanguage = in.readString();
    pasteData = (Calendar) in.readSerializable();
    pasteKey = in.readString();
  }

  public PasteInfo(String pasteName, String pasteAuthor, String pasteLanguage, Calendar pasteData,
                   String pasteKey) {
    this.pasteName = pasteName;
    this.pasteAuthor = pasteAuthor;
    this.pasteLanguage = pasteLanguage;
    this.pasteData = pasteData;
    this.pasteKey = pasteKey;
  }

  public PasteInfo() {
  }

  public String getPasteName() {
    return pasteName;
  }

  public void setPasteName(String pasteName) {
    this.pasteName = pasteName;
  }

  public String getPasteAuthor() {
    return pasteAuthor;
  }

  public void setPasteAuthor(String pasteAuthor) {
    this.pasteAuthor = pasteAuthor;
  }

  public String getPasteLanguage() {
    return pasteLanguage;
  }

  public void setPasteLanguage(String pasteLanguage) {
    this.pasteLanguage = pasteLanguage;
  }

  public Calendar getPasteData() {
    return pasteData;
  }

  public void setPasteData(Calendar pasteData) {
    this.pasteData = pasteData;
  }

  public String getPasteKey() {
    return pasteKey;
  }

  public void setPasteKey(String pasteKey) {
    this.pasteKey = pasteKey;
  }

  public int getSqlID() {
    return sqlID;
  }

  public void setSqlID(int sqlID) {
    this.sqlID = sqlID;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(sqlID);
    parcel.writeString(pasteName);
    parcel.writeString(pasteAuthor);
    parcel.writeString(pasteLanguage);
    parcel.writeSerializable(pasteData);
    parcel.writeString(pasteKey);
  }

//    public void delete()
//    {
//
//    }
}
