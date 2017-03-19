package com.rutgerssustainability.android.rutgerssustainability.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;

/**
 * Created by shreyashirday on 1/26/17.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    //table creation strings
    private static final String CREATE_TRASH_TABLE = "create table "
            + Constants.DB.TABLE_TRASH + "(" + Constants.DB.COLUMN_TRASH_ID + " text primary key, "
            + Constants.DB.COLUMN_JSON + " text);";

    private static final String CREATE_NOISE_TABLE = "create table "
            + Constants.DB.TABLE_NOISE + "(" + Constants.DB.COLUMN_NOISE_ID + " text primary key, "
            + Constants.DB.COLUMN_JSON + " text);";


    private static final String DROP_TRASH_TABLE = "DROP TABLE IF EXISTS " + Constants.DB.TABLE_TRASH;

    private static final String DROP_NOISE_TABLE = "DROP TABLE IF EXISTS " + Constants.DB.TABLE_NOISE;

    public SQLiteHelper(final Context context) {
        super(context, Constants.DB.DB_NAME,null,Constants.DB.DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(CREATE_TRASH_TABLE);
        db.execSQL(CREATE_NOISE_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL(DROP_TRASH_TABLE);
        db.execSQL(DROP_NOISE_TABLE);
        onCreate(db);
    }
}
