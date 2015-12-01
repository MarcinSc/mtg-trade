package com.gempukku.mtg.trader.service.db;

import android.provider.BaseColumns;

public final class TradeStorageContract {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";

    public static final String SQL_CREATE_TRADE_INFO =
            "CREATE TABLE " + TradeInfoEntry.TABLE_NAME + " (" +
                    TradeInfoEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    TradeInfoEntry.COLUMN_NAME_DATE + " INTEGER" +
                    " )";
    public static final String SQL_CREATE_TRADE_ENTRY =
            "CREATE TABLE " + TradeEntry.TABLE_NAME + " (" +
                    TradeEntry._ID + " INTEGER PRIMARY KEY" +
                    COMMA_SEP + TradeEntry.COLUMN_NAME_TYPE + " INTEGER" +
                    COMMA_SEP + TradeEntry.COLUMN_NAME_CARD_ID + TEXT_TYPE +
                    COMMA_SEP + TradeEntry.COLUMN_NAME_PRICE + " INTEGER" +
                    COMMA_SEP + TradeEntry.COLUMN_NAME_COUNT + " INTEGER" +
                    COMMA_SEP + TradeEntry.COLUMN_NAME_MULTIPLIER + " REAL" +
                    COMMA_SEP + TradeEntry.COLUMN_NAME_TRADE_INFO + " INTEGER" +
                    COMMA_SEP + "FOREIGN KEY(" + TradeEntry.COLUMN_NAME_TRADE_INFO + ") REFERENCES " + TradeInfoEntry.TABLE_NAME + "(" + TradeInfoEntry._ID + ")" +
                    " )";

    private TradeStorageContract() {
    }

    public static abstract class TradeInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "tradeInfo";
        public static final String COLUMN_NAME_DATE = "tradeDate";
    }

    public static abstract class TradeEntry implements BaseColumns {
        public static final String TABLE_NAME = "tradeEntry";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_CARD_ID = "cardId";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_COUNT = "count";
        public static final String COLUMN_NAME_MULTIPLIER = "multiplier";
        public static final String COLUMN_NAME_TRADE_INFO = "tradeInfoId";
    }
}