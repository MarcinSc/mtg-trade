package com.gempukku.mtg.trader.service.db.card;

import android.provider.BaseColumns;

public final class CardProviderContract {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";

    public static final String SQL_CREATE_INFO =
            "CREATE TABLE " + InfoEntry.TABLE_NAME + " (" +
                    InfoEntry._ID + " INTEGER PRIMARY KEY" +
                    COMMA_SEP + InfoEntry.COLUMN_KEY + TEXT_TYPE +
                    COMMA_SEP + InfoEntry.COLUMN_VALUE + TEXT_TYPE +
                    " )";
    public static final String SQL_CREATE_CARD =
            "CREATE TABLE " + CardEntry.TABLE_NAME + " (" +
                    CardEntry._ID + " INTEGER PRIMARY KEY" +
                    COMMA_SEP + CardEntry.COLUMN_CARD_ID + TEXT_TYPE +
                    COMMA_SEP + CardEntry.COLUMN_NAME + TEXT_TYPE +
                    COMMA_SEP + CardEntry.COLUMN_INFO + TEXT_TYPE +
                    COMMA_SEP + CardEntry.COLUMN_PRICE + " INTEGER" +
                    COMMA_SEP + CardEntry.COLUMN_LINK + TEXT_TYPE +
                    " )";

    public static final String SQL_INIT_INFO =
            "INSERT INTO " + InfoEntry.TABLE_NAME + " (" + InfoEntry.COLUMN_KEY + ", " + InfoEntry.COLUMN_VALUE + ") VALUES " +
                    "('update_date', '0')";


    private CardProviderContract() {
    }

    public static abstract class InfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "info";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_VALUE = "value";
    }

    public static abstract class CardEntry implements BaseColumns {
        public static final String TABLE_NAME = "card";
        public static final String COLUMN_CARD_ID = "cardId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_INFO = "info";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_LINK = "link";
    }
}