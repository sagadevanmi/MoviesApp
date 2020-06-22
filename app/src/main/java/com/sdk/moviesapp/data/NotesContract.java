package com.sdk.moviesapp.data;

import android.provider.BaseColumns;

public class NotesContract {

    public static final class NotesEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_MOVIEID = "movieid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTERPATH = "posterpath";
        public static final String COLUMN_NOTE = "note";
    }
}
