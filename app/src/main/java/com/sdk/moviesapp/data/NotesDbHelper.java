package com.sdk.moviesapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sdk.moviesapp.model.Notes;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class NotesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;
    public static final String LOGTAG = "NOTES";

    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    public NotesDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public NotesDbHelper(@Nullable Context context) {
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
        final String SQL_CREATE_FAVOURITE_TABLE = "CREATE TABLE " + NotesContract.NotesEntry.TABLE_NAME + " (" +
                NotesContract.NotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NotesContract.NotesEntry.COLUMN_MOVIEID + " INTEGER, " +
                NotesContract.NotesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                NotesContract.NotesEntry.COLUMN_POSTERPATH + " TEXT NOT NULL, " +
                NotesContract.NotesEntry.COLUMN_NOTE + " TEXT NOT NULL " + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NotesContract.NotesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addNote(Notes note) {
        int id = note.getId();
        SQLiteDatabase read = this.getReadableDatabase();
        Cursor res = read.query(NotesContract.NotesEntry.TABLE_NAME, new String[]{NotesContract.NotesEntry.COLUMN_MOVIEID},
                "movieid = " + id, null, null, null , null);
        if (res.getCount() < 1) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NotesContract.NotesEntry.COLUMN_MOVIEID, note.getId());
            values.put(NotesContract.NotesEntry.COLUMN_TITLE, note.getTitle());
            values.put(NotesContract.NotesEntry.COLUMN_POSTERPATH, note.getPosterPath());
            values.put(NotesContract.NotesEntry.COLUMN_NOTE, note.getNote());

            db.insert(NotesContract.NotesEntry.TABLE_NAME, null, values);
            db.close();
        }
    }

    public void deleteNote (int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(NotesContract.NotesEntry.TABLE_NAME, NotesContract.NotesEntry.COLUMN_MOVIEID + " = " + id, null);
        db.close();
    }

    public List<Notes> fetch () {
        List<Notes> list = new ArrayList<>();
        String[] columns = {
                NotesContract.NotesEntry._ID,
                NotesContract.NotesEntry.COLUMN_MOVIEID,
                NotesContract.NotesEntry.COLUMN_TITLE,
                NotesContract.NotesEntry.COLUMN_POSTERPATH,
                NotesContract.NotesEntry.COLUMN_NOTE
        };

        String sortOrder = NotesContract.NotesEntry._ID + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(NotesContract.NotesEntry.TABLE_NAME, columns, null, null, null, null, sortOrder);

        if (cursor.moveToFirst()) {
            do {
                Notes movie = new Notes();
                movie.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_MOVIEID))));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE)));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_POSTERPATH)));
                movie.setNote(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_NOTE)));

                list.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

}
