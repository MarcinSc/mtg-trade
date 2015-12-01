package com.gempukku.mtg.trader.service.db.card;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CardProviderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CardProvider.db";

    public CardProviderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CardProviderContract.SQL_CREATE_INFO);
        db.execSQL(CardProviderContract.SQL_CREATE_CARD);

        db.execSQL(CardProviderContract.SQL_INIT_INFO);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing yet, this is first version
    }
}