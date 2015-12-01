package com.gempukku.mtg.trader.service.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gempukku.mtg.trader.dao.TradeInfo;
import com.gempukku.mtg.trader.service.TradeStorage;

import java.util.LinkedList;
import java.util.List;

import static com.gempukku.mtg.trader.service.db.TradeStorageContract.TradeEntry;
import static com.gempukku.mtg.trader.service.db.TradeStorageContract.TradeInfoEntry;

public class DbTradeStorage implements TradeStorage {
    private static final int MINE_ENTRY_TYPE = 0;
    private static final int THEIR_ENTRY_TYPE = 1;

    private TradeStorageDbHelper _dbHelper;

    public DbTradeStorage(Context context) {
        _dbHelper = new TradeStorageDbHelper(context);
    }

    @Override
    public List<TradeInfo> listTrades() {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT "
                + "i." + TradeInfoEntry._ID
                + ", i." + TradeInfoEntry.COLUMN_NAME_DATE
                + ", e." + TradeEntry.COLUMN_NAME_TYPE
                + ", e." + TradeEntry.COLUMN_NAME_CARD_ID
                + ", e." + TradeEntry.COLUMN_NAME_COUNT
                + ", e." + TradeEntry.COLUMN_NAME_PRICE
                + ", e." + TradeEntry.COLUMN_NAME_MULTIPLIER
                + " FROM "
                + TradeInfoEntry.TABLE_NAME + " i INNER JOIN "
                + TradeEntry.TABLE_NAME + " e ON i." + TradeInfoEntry._ID + "=e." + TradeEntry.COLUMN_NAME_TRADE_INFO
                + " ORDER BY i." + TradeInfoEntry.COLUMN_NAME_DATE + " desc, e." + TradeEntry._ID + " asc", new String[0]);

        List<TradeInfo> result = new LinkedList<TradeInfo>();
        try {

            Integer lastTradeId = null;
            TradeInfo lastTradeInfo = null;

            while (cursor.moveToNext()) {
                int tradeId = cursor.getInt(0);
                if (lastTradeId == null || tradeId != lastTradeId) {
                    lastTradeId = tradeId;
                    lastTradeInfo = new TradeInfo(cursor.getLong(1));
                    result.add(lastTradeInfo);
                }
                String cardId = cursor.getString(3);
                int count = cursor.getInt(4);
                int price = cursor.getInt(5);
                float multiplier = cursor.getFloat(6);
                if (cursor.getInt(2) == MINE_ENTRY_TYPE) {
                    lastTradeInfo.addMineCard(cardId, count, price, multiplier);
                } else {
                    lastTradeInfo.addTheirCard(cardId, count, price, multiplier);
                }
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    @Override
    public void storeTrade(TradeInfo trade) {
        SQLiteDatabase db = _dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues infoValues = new ContentValues();
            infoValues.put(TradeInfoEntry.COLUMN_NAME_DATE, trade.getDate());

            long infoId = db.insert(TradeInfoEntry.TABLE_NAME, null, infoValues);

            insertEntries(db, infoId, MINE_ENTRY_TYPE, trade.getMyCards());
            insertEntries(db, infoId, THEIR_ENTRY_TYPE, trade.getTheirCards());

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int getHistoricalProfit() {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();

        int mineTotal = 0;
        int theirTotal = 0;

        Cursor cursor = db.query(TradeEntry.TABLE_NAME, new String[]{TradeEntry.COLUMN_NAME_TYPE, "sum(" + TradeEntry.COLUMN_NAME_PRICE + "*" + TradeEntry.COLUMN_NAME_COUNT + "*" + TradeEntry.COLUMN_NAME_MULTIPLIER + ")"},
                null, null, TradeEntry.COLUMN_NAME_TYPE, null, null);
        try {
            while (cursor.moveToNext()) {
                int type = cursor.getInt(0);
                float value = cursor.getFloat(1);
                if (type == MINE_ENTRY_TYPE)
                    mineTotal = Math.round(value);
                else
                    theirTotal = Math.round(value);
            }
        } finally {
            cursor.close();
        }
        return theirTotal - mineTotal;
    }

    private void insertEntries(SQLiteDatabase db, long infoId, int type, Iterable<com.gempukku.mtg.trader.dao.TradeEntry> cardEntries) {
        for (com.gempukku.mtg.trader.dao.TradeEntry cardEntry : cardEntries) {
            ContentValues entryValues = new ContentValues();
            entryValues.put(TradeStorageContract.TradeEntry.COLUMN_NAME_TRADE_INFO, infoId);
            entryValues.put(TradeStorageContract.TradeEntry.COLUMN_NAME_TYPE, type);
            entryValues.put(TradeStorageContract.TradeEntry.COLUMN_NAME_CARD_ID, cardEntry.getCardId());
            entryValues.put(TradeStorageContract.TradeEntry.COLUMN_NAME_COUNT, cardEntry.getCount());
            entryValues.put(TradeStorageContract.TradeEntry.COLUMN_NAME_PRICE, cardEntry.getPrice());
            entryValues.put(TradeStorageContract.TradeEntry.COLUMN_NAME_MULTIPLIER, cardEntry.getMultiplier());

            db.insert(TradeStorageContract.TradeEntry.TABLE_NAME, null, entryValues);
        }
    }
}
