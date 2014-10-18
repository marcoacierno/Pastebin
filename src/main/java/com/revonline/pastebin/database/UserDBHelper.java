package com.revonline.pastebin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class UserDBHelper extends SQLiteOpenHelper implements DBData {

  private static final int DATABASE_VERSION = 1;
  private static final String KEY_USERNAME = "username";
  private static final String KEY_USERKEY = "key";
  private static final String TABLE_NAME = "user";
  private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "\n" +
                                             "(\n" +
                                             KEY_USERNAME + " text,\n" +
                                             KEY_USERKEY + " text\n" +
                                             ")";
  private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

  public UserDBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Anche se non supportato al momento, con questo sistema è possibile far loggare più utenti nello stesso momento
  public void addUser(String name, String key) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(KEY_USERNAME, name);
    values.put(KEY_USERKEY, key);

    db.insert(TABLE_NAME, null, values);
    db.close();
  }  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int i, int i2) {
    db.execSQL(DROP_TABLE);
    onCreate(db);
  }


}
