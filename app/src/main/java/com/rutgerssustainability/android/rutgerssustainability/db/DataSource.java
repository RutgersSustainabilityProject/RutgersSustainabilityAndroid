package com.rutgerssustainability.android.rutgerssustainability.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shreyashirday on 1/26/17.
 */
public class DataSource {
    //sql lite objects
    private SQLiteDatabase db;
    private SQLiteHelper sqLiteHelper;

    //gson
    private Gson gson;

    public DataSource(final Context context) {
        sqLiteHelper = new SQLiteHelper(context);
        gson = new Gson();
    }

    public void open() throws SQLException {
        db = sqLiteHelper.getWritableDatabase();
    }

    public void close() {
        sqLiteHelper.close();
    }

    public void addTrash(final Trash trash) {
        final String json = gson.toJson(trash);
        final String insertCommand = "insert into " + Constants.DB.TABLE_TRASH
                + " VALUES('" + trash.getUniqueId() + "', '" + json + "');";
        db.execSQL(insertCommand);
    }

    public boolean hasTrash(final Trash trash) {
        final String selectCommand = "SELECT * FROM " + Constants.DB.TABLE_TRASH +
               " WHERE " + Constants.DB.COLUMN_TRASH_ID + " = '" + trash.getUniqueId() + "'";
        final Cursor cursor = db.rawQuery(selectCommand, null);
        final boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public Trash getTrash(final String trashId) {
        final String selectCommand = "SELECT * FROM " + Constants.DB.TABLE_TRASH +
                " WHERE " + Constants.DB.COLUMN_TRASH_ID + " = '" + trashId + "'";
        final Cursor cursor = db.rawQuery(selectCommand,null);
        if (cursor.moveToFirst()) {
            final String json = cursor.getString(1);
            final Trash trash = gson.fromJson(json, Trash.class);
            cursor.close();
            return trash;
        }
        return null;
    }

    public boolean doesOldTrashExist(final Trash[] trashs) {
        final String selectCommand = "SELECT * FROM " + Constants.DB.TABLE_TRASH;
        final Cursor cursor = db.rawQuery(selectCommand, null);
        final List<String> trashIdList = new ArrayList<>();
        for (Trash t : trashs) {
            trashIdList.add(t.getUniqueId());
        }
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                final String trashId = cursor.getString(0);
                if (!trashIdList.contains(trashId)) {
                    cursor.close();
                    return true;
                }
            }
        }
        cursor.close();
        return false;
    }

    public List<Trash> getAllTrash() {
        final String selectCommand = "SELECT * FROM " + Constants.DB.TABLE_TRASH;
        final Cursor cursor = db.rawQuery(selectCommand, null);
        final List<Trash> trashList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                final String json = cursor.getString(1);
                final Trash trash = gson.fromJson(json,Trash.class);
                trashList.add(trash);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return trashList;
    }

   public void deleteTrashTable() {
       db.delete(Constants.DB.TABLE_TRASH,null,null);
   }

}
