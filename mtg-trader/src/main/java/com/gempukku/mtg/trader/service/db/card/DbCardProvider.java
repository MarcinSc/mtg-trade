package com.gempukku.mtg.trader.service.db.card;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;
import com.gempukku.mtg.trader.service.db.card.CardProviderContract.CardEntry;
import com.gempukku.mtg.trader.service.db.card.CardProviderContract.InfoEntry;

import java.util.LinkedList;
import java.util.List;

public class DbCardProvider implements CardProvider {
    private static final long OUTDATED_PERIOD = 3 * 24 * 60 * 60 * 1000; // 3 days

    private CardDataSource _cardDataSource;

    private CardProviderDbHelper _dbHelper;

    public DbCardProvider(Context context, CardDataSource cardDataSource) {
        _cardDataSource = cardDataSource;
        _dbHelper = new CardProviderDbHelper(context);
    }

    @Override
    public Iterable<CardInfo> findCards(String text, int maxCount) {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CardEntry.TABLE_NAME,
                new String[]{CardEntry.COLUMN_CARD_ID, CardEntry.COLUMN_NAME, CardEntry.COLUMN_INFO, CardEntry.COLUMN_PRICE},
                "instr(" + CardEntry.COLUMN_NAME + ", ?) > 0", new String[]{text}, null, null, CardEntry.COLUMN_NAME, String.valueOf(maxCount));
        List<CardInfo> result = new LinkedList<CardInfo>();
        try {
            while (cursor.moveToNext()) {
                result.add(new CardInfo(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    @Override
    public long getDatabaseUpdateDate() {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = db.query(InfoEntry.TABLE_NAME,
                new String[]{InfoEntry.COLUMN_VALUE},
                InfoEntry.COLUMN_KEY + "=?", new String[]{"update_date"}, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    @Override
    public boolean isDatabaseOutdated() {
        long updateDate = getDatabaseUpdateDate();
        return updateDate + OUTDATED_PERIOD < System.currentTimeMillis();
    }

    @Override
    public CancellableUpdate updateDatabase(ProgressUpdate progressUpdate, Runnable finishCallback) {
        return _cardDataSource.updateInBackground(progressUpdate, new DbCardStorage(), finishCallback);
    }

    @Override
    public CardInfo getCardById(String cardId) {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CardEntry.TABLE_NAME,
                new String[]{CardEntry.COLUMN_CARD_ID, CardEntry.COLUMN_NAME, CardEntry.COLUMN_INFO, CardEntry.COLUMN_PRICE},
                CardEntry.COLUMN_CARD_ID + "=?", new String[]{cardId}, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return new CardInfo(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private class DbCardStorage implements CardDataSource.CardStorage {
        private SQLiteDatabase _db;

        @Override
        public void startStoring() {
            _db = _dbHelper.getWritableDatabase();
            _db.beginTransaction();

            _db.delete(CardEntry.TABLE_NAME, null, null);
        }

        @Override
        public void storeCard(CardInfo cardInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CardEntry.COLUMN_CARD_ID, cardInfo.getId());
            contentValues.put(CardEntry.COLUMN_NAME, cardInfo.getName());
            contentValues.put(CardEntry.COLUMN_INFO, cardInfo.getVersionInfo());
            contentValues.put(CardEntry.COLUMN_PRICE, cardInfo.getPrice());

            _db.insert(CardEntry.TABLE_NAME, null, contentValues);
        }

        @Override
        public void finishStoring(boolean success) {
            try {
                if (success) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InfoEntry.COLUMN_VALUE, System.currentTimeMillis());
                    _db.update(InfoEntry.TABLE_NAME, contentValues, InfoEntry.COLUMN_KEY + "=?", new String[]{"update_date"});

                    _db.setTransactionSuccessful();
                }
            } finally {
                _db.endTransaction();
            }
        }
    }
}
