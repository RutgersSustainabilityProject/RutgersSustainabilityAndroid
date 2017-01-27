package com.rutgerssustainability.android.rutgerssustainability.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;

import java.sql.SQLException;

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

   public void deleteTrashTable() {
       db.delete(Constants.DB.TABLE_TRASH,null,null);
   }

}
