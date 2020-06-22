package com.sdk.moviesapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sdk.moviesapp.model.Movie;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class FavoriteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite.db";
    private static final int DATABASE_VERSION = 1;
    public static final String LOGTAG = "FAVORITE";

    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    public FavoriteDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public FavoriteDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open() {
        Log.d(LOGTAG, "Database Opened");
        db = dbHandler.getWritableDatabase();
    }

    public void close() {
        Log.d(LOGTAG, "Database Closed");
        dbHandler.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVOURITE_TABLE = "CREATE TABLE " + FavoriteContract.FavoriteEntry.TABLE_NAME + " (" +
                FavoriteContract.FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteContract.FavoriteEntry.COLUMN_MOVIEID + " INTEGER, " +
                FavoriteContract.FavoriteEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoriteContract.FavoriteEntry.COLUMN_USERRATING + " TEXT NOT NULL, " +
                FavoriteContract.FavoriteEntry.COLUMN_POSTERPATH + " TEXT NOT NULL, " +
                FavoriteContract.FavoriteEntry.COLUMN_PLOTSYNOPSIS + " TEXT NOT NULL " + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteContract.FavoriteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addFavorite (Movie movie, Context context) {
        int id = movie.getId();
        SQLiteDatabase read = this.getReadableDatabase();
        Cursor res = read.query(FavoriteContract.FavoriteEntry.TABLE_NAME, new String[]{FavoriteContract.FavoriteEntry.COLUMN_MOVIEID},
                "movieid = " + id, null, null, null , null);
        if (res.getCount() < 1) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIEID, movie.getId());
            values.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, movie.getOriginalTitle());
            values.put(FavoriteContract.FavoriteEntry.COLUMN_USERRATING, movie.getVoteaverage());
            values.put(FavoriteContract.FavoriteEntry.COLUMN_POSTERPATH, movie.getPosterPath());
            values.put(FavoriteContract.FavoriteEntry.COLUMN_PLOTSYNOPSIS, movie.getOverview());

            db.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, values);
            db.close();
            return true;
        }
        //Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();
        return false;
    }

    public void deleteFavorite (int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(FavoriteContract.FavoriteEntry.TABLE_NAME, FavoriteContract.FavoriteEntry.COLUMN_MOVIEID + " = " + id, null);
    }

    public List<Movie> fetch () {
        List<Movie> list = new ArrayList<>();
        String[] columns = {
                FavoriteContract.FavoriteEntry._ID,
                FavoriteContract.FavoriteEntry.COLUMN_MOVIEID,
                FavoriteContract.FavoriteEntry.COLUMN_TITLE,
                FavoriteContract.FavoriteEntry.COLUMN_USERRATING,
                FavoriteContract.FavoriteEntry.COLUMN_POSTERPATH,
                FavoriteContract.FavoriteEntry.COLUMN_PLOTSYNOPSIS
        };

        String sortOrder = FavoriteContract.FavoriteEntry._ID + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(FavoriteContract.FavoriteEntry.TABLE_NAME, columns, null, null, null, null, sortOrder);

        if (cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIEID))));
                movie.setOriginalTitle(cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_TITLE)));
                movie.setVoteaverage(Double.parseDouble(cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_USERRATING))));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_POSTERPATH)));
                movie.setOverview(cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_PLOTSYNOPSIS)));

                list.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
