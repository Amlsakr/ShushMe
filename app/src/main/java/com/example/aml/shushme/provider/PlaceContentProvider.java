package com.example.aml.shushme.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by aml on 15/04/18.
 */

public class PlaceContentProvider extends ContentProvider {

    public static final int PLACES = 100 ;
    public static final int PLACE_WITH_ID = 101 ;
    private static UriMatcher sUriMatcher = buildUriMatcher();
   private static final String TAG = PlaceContentProvider.class.getSimpleName();
    private PlaceDBHelper placeDBHelper ;

    public static UriMatcher buildUriMatcher (){

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PlaceContract.PlaceEntry.AUTHORITY , PlaceContract.PlaceEntry.PATH_PLACES , PLACES);
        uriMatcher.addURI(PlaceContract.PlaceEntry.AUTHORITY , PlaceContract.PlaceEntry.PATH_PLACES +  "/#" ,PLACE_WITH_ID);
        return uriMatcher ;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        placeDBHelper = new PlaceDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = placeDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCuesor ;
        switch (match) {
            case PLACES  :
                retCuesor = db.query(PlaceContract.PlaceEntry.TABLE_NAME , projection
                , selection , selectionArgs , null , null , sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("UNKNOWN URI : " + uri) ;

        }


        // Set a notification URI on the  cuesor and return that cursor
        retCuesor.setNotificationUri(getContext().getContentResolver() , uri);
        return retCuesor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = placeDBHelper.getWritableDatabase();
        int match  = sUriMatcher.match(uri);
        Uri returnUri ;
        switch ( match) {
            case PLACES :
                Long id = db.insert(PlaceContract.PlaceEntry.TABLE_NAME ,null ,values );
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(PlaceContract.PlaceEntry.CONTENT_URI , id);
                } else  {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw  new UnsupportedOperationException("UNKNOWN uri:  " + uri);
        }

        getContext().getContentResolver().notifyChange(uri , null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final  SQLiteDatabase db = placeDBHelper.getWritableDatabase() ;
        int match = sUriMatcher.match(uri);
        int placeDElet ;
        switch (match) {
            case PLACE_WITH_ID :
                String id = uri.getPathSegments().get(1);
                placeDElet = db.delete(PlaceContract.PlaceEntry.TABLE_NAME , "_id=?" , new String[] {id});
                break;
            default:
           throw      new UnsupportedOperationException(" UNKNOWN URI : " + uri);

        }
        if (placeDElet != 0){
            getContext().getContentResolver().notifyChange(uri , null);
        }
        return placeDElet;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = placeDBHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int placesUpdated ;
        switch (match) {
            case PLACE_WITH_ID :
                String id = uri.getPathSegments().get(1);
                placesUpdated = db.update(PlaceContract.PlaceEntry.TABLE_NAME ,values , "_id=?" ,
                        new String[]{id});
                break;
            default:
                throw  new UnsupportedOperationException("UNKNOWN URI : " + uri);

        }
        if (placesUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri , null);
        }
        return placesUpdated;
    }
}
