package com.gempukku.mtg.trader.service.db.trade;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TradeStorageDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TradeStorage.db";

    public TradeStorageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TradeStorageContract.SQL_CREATE_TRADE_INFO);
        db.execSQL(TradeStorageContract.SQL_CREATE_TRADE_ENTRY);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing yet, this is first version
    }
}