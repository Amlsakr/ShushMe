package com.example.aml.shushme.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by aml on 15/04/18.
 */

public class PlaceDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shushme.db";
    private static final int VERSION = 1 ;

    public PlaceDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

       // Create a table to hold the places data
//        final String SQL_CREATE_PLACES_TABLE = "CREATE TABLE " + PlaceContract.PlaceEntry.TABLE_NAME + " (" +
//                PlaceContract.PlaceEntry._ID + " INTEGER PRIMARY KEY, " +
//                PlaceContract.PlaceEntry.COLUMN_PLACE_ID + "TEXT NOT NULL);"  ;

        final String CREATE_TABLE =
                "CREATE TABLE " + PlaceContract.PlaceEntry.TABLE_NAME + " (" +
                        PlaceContract.PlaceEntry._ID + " INTEGER PRIMARY KEY, " +
                        PlaceContract.PlaceEntry.COLUMN_PLACE_ID + " Text NOT NULL);";

//        final String CREATE_TABLE =
//                "CREATE TABLE " + PlaceContract.PlaceEntry.TABLE_NAME + " (" +
//                        PlaceContract.PlaceEntry._ID + " INTEGER PRIMARY KEY, " +
//                        PlaceContract.PlaceEntry.COLUMN_PLACE_ID + " INTEGER NOT NULL);";

        db.execSQL(CREATE_TABLE);
        Log.e("SQLITECREATE" , "create successfully");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + PlaceContract.PlaceEntry.TABLE_NAME);
        onCreate(db);


    }
}
