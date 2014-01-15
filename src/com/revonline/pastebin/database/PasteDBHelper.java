package com.revonline.pastebin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.revonline.pastebin.MyActivity;
import com.revonline.pastebin.PasteInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 30/11/13
 * Time: 22.06
 * To change this template use File | Settings | File Templates.
 */
public class PasteDBHelper extends SQLiteOpenHelper implements DBData {
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "pastes";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_SCADENZA = "scadenza";
    private static final String KEY_TIPO = "tipo";
    private static final String KEY_KEY = "url";
    private static final String KEY_TIME = "time";
    private static final String CREATE_TABLE = "create table "+TABLE_NAME+"\n" +
            "(\n" +
            KEY_ID + " integer primary key autoincrement,\n" +
            KEY_NAME + " text not null,\n" +
            KEY_LANGUAGE + " text not null,\n" +
            KEY_SCADENZA + " text not null,\n" +
            KEY_TIPO + " int,\n" +
            KEY_TIME + " int,\n" + // milliseconds
            KEY_KEY + " text not null\n" +
            ")";
    private static final String DROP_TABLE = "drop table if exists "+TABLE_NAME;

    public PasteDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //To change body of implemented methods use File | Settings | File Templates.
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(MyActivity.DEBUG_TAG, "onUpgrade, oldVersion: " + oldVersion + ", newVersion: " + newVersion);
        //To change body of implemented methods use File | Settings | File Templates.
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void addPaste(String name, String language, String scadenza, int tipo, String key)
    {
        Log.d(MyActivity.DEBUG_TAG, "addPaste with: " + name + ", " + language + ", " + scadenza + ", " + tipo + ", " + key);

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, name);
        contentValues.put(KEY_LANGUAGE, language);
        contentValues.put(KEY_SCADENZA, scadenza);
        contentValues.put(KEY_TIPO, tipo);
        contentValues.put(KEY_KEY, key);

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public List<PasteInfo> getAllPastes()
    {
        Log.d(MyActivity.DEBUG_TAG, "getAllPastes");

        SQLiteDatabase db = getReadableDatabase();
        /**table	The table name to compile the query against.
         columns	A list of which columns to return. Passing null will return all columns, which is discouraged to prevent reading data from storage that isn't going to be used.
         selection	A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
         selectionArgs	You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
         groupBy	A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself). Passing null will cause the rows to not be grouped.
         having	A filter declare which row groups to include in the cursor, if row grouping is being used, formatted as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all row groups to be included, and is required when row grouping is not being used.
         orderBy	How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
         limit	Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

         public Cursor query (String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)*/
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_ID, KEY_NAME, KEY_LANGUAGE, KEY_SCADENZA, KEY_TIPO, KEY_KEY, KEY_TIME}, null, null, null, null, null, null );

        Log.d(MyActivity.DEBUG_TAG, "Count = " + cursor.getCount());

        //String pasteName, String pasteAuthor, String pasteLanguage, GregorianCalendar pasteData, String pasteKey
        List<PasteInfo> list = new ArrayList<PasteInfo>();

        if (cursor.moveToFirst())
        {
            PasteInfo pasteInfo;

            int idxID = cursor.getColumnIndex(KEY_ID);
            int idxName = cursor.getColumnIndex(KEY_NAME);
            int idxLang = cursor.getColumnIndex(KEY_LANGUAGE);
            int idxKey = cursor.getColumnIndex(KEY_KEY);
            int idxTime = cursor.getColumnIndex(KEY_TIME);

            do {
                pasteInfo = new PasteInfo();

                pasteInfo.setPasteName(cursor.getString(idxName));
                pasteInfo.setPasteLanguage(cursor.getString(idxLang));
                pasteInfo.setSqlID(cursor.getInt(idxID));
                pasteInfo.setPasteKey(cursor.getString(idxKey));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(cursor.getInt(idxTime)*1000);
                pasteInfo.setPasteData(calendar);

                list.add(pasteInfo);

                Log.d(MyActivity.DEBUG_TAG, "moveToNext");
            } while(cursor.moveToNext());
        }
        else
        {
            Log.d(MyActivity.DEBUG_TAG, "moveToFirst returns false");
        }

        return list;
    }
}
