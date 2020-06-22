package com.sdk.moviesapp.data;

import android.provider.BaseColumns;

public class FavoriteContract {

    public static final class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorite";
        public static final String COLUMN_MOVIEID = "movieid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_USERRATING = "userrating";
        public static final String COLUMN_POSTERPATH = "posterpath";
        public static final String COLUMN_PLOTSYNOPSIS = "overview";
        //public static final String COLUMN_NOTE = "note";
    }
}

