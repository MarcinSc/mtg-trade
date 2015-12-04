package com.gempukku.mtg.trader.service.db.card;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.gempukku.mtg.trader.MtgTraderApplication;
import com.gempukku.mtg.trader.R;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;
import com.gempukku.mtg.trader.service.db.card.CardProviderContract.CardEntry;
import com.gempukku.mtg.trader.service.db.card.CardProviderContract.InfoEntry;

import java.util.Collections;
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
        if (text.trim().length() == 0)
            return Collections.emptyList();
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CardEntry.TABLE_NAME,
                new String[]{CardEntry.COLUMN_CARD_ID, CardEntry.COLUMN_NAME, CardEntry.COLUMN_INFO, CardEntry.COLUMN_PRICE},
                "instr(lower(" + CardEntry.COLUMN_NAME + "), ?) > 0", new String[]{text.toLowerCase()}, null, null, CardEntry.COLUMN_NAME, String.valueOf(maxCount));
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
    public CancellableUpdate updateDatabase(ProgressUpdate progressUpdate, UpdateResult finishCallback) {
        return _cardDataSource.updateInBackground(new DbCardStorage(progressUpdate, finishCallback));
    }

    @Override
    public DbCardInfo getCardById(String cardId) {
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CardEntry.TABLE_NAME,
                new String[]{CardEntry.COLUMN_CARD_ID, CardEntry.COLUMN_NAME, CardEntry.COLUMN_INFO, CardEntry.COLUMN_PRICE, CardEntry.COLUMN_LINK},
                CardEntry.COLUMN_CARD_ID + "=?", new String[]{cardId}, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return new DbCardInfo(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public View getCardDetailsScreen(final Context context, String cardId) {
        final DbCardInfo cardInfo = getCardById(cardId);

        LayoutInflater vi = LayoutInflater.from(context);
        View rootView = vi.inflate(R.layout.tcg_cardinfo_fragment, null);

        TextView nameView = (TextView) rootView.findViewById(R.id.name);
        nameView.setText(cardInfo.getName());

        TextView infoView = (TextView) rootView.findViewById(R.id.info);
        infoView.setText(cardInfo.getVersionInfo());

        TextView priceView = (TextView) rootView.findViewById(R.id.price);
        priceView.setText(MtgTraderApplication.formatPrice(cardInfo.getPrice()));

        View goToSiteButton = rootView.findViewById(R.id.goToSite);
        final String link = cardInfo.getLink();
        if (link != null) {
            goToSiteButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                context.startActivity(myIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "No application can handle this request."
                                        + " Please install a web browser", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            goToSiteButton.setEnabled(false);
        }

        return rootView;
    }

    private class DbCardStorage implements CardDataSource.CardStorage {
        private ProgressUpdate _progressUpdate;
        private UpdateResult _updateResult;

        private SQLiteDatabase _db;
        private boolean _startedTransaction;
        private int _storedCount;
        private int _maxCount;

        public DbCardStorage(ProgressUpdate progressUpdate, UpdateResult updateResult) {
            _progressUpdate = progressUpdate;
            _updateResult = updateResult;
        }

        @Override
        public void startStoring(int max) {
            _db = _dbHelper.getWritableDatabase();
            _db.beginTransaction();
            _startedTransaction = true;

            _db.delete(CardEntry.TABLE_NAME, null, null);
            _maxCount = max;
            _progressUpdate.updateProgress(0, _maxCount);
        }

        @Override
        public void storeCard(CardInfo cardInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CardEntry.COLUMN_CARD_ID, cardInfo.getId());
            contentValues.put(CardEntry.COLUMN_NAME, cardInfo.getName());
            contentValues.put(CardEntry.COLUMN_INFO, cardInfo.getVersionInfo());
            contentValues.put(CardEntry.COLUMN_PRICE, cardInfo.getPrice());

            _db.insert(CardEntry.TABLE_NAME, null, contentValues);
            _storedCount++;
            _progressUpdate.updateProgress(_storedCount, _maxCount);
        }

        @Override
        public void cancelled() {
            try {
                if (_startedTransaction) {
                    _db.endTransaction();
                }
            } finally {
                _updateResult.cancelled();
            }
        }

        @Override
        public void finishStoring() {
            try {
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InfoEntry.COLUMN_VALUE, System.currentTimeMillis());
                    _db.update(InfoEntry.TABLE_NAME, contentValues, InfoEntry.COLUMN_KEY + "=?", new String[]{"update_date"});

                    _db.setTransactionSuccessful();
                } finally {
                    _db.endTransaction();
                }
            } finally {
                _updateResult.success();
            }
        }

        @Override
        public void error(String message) {
            try {
                if (_startedTransaction) {
                    _db.endTransaction();
                }
            } finally {
                _updateResult.error(message);
            }
        }
    }
}
