package com.gempukku.mtg.trader.service;

import android.content.Context;
import android.view.View;
import com.gempukku.mtg.trader.dao.CardInfo;

public interface CardProvider {
    long getDatabaseUpdateDate();

    boolean isDatabaseOutdated();

    CancellableUpdate updateDatabase(ProgressUpdate progressUpdate, UpdateResult finishCallback);

    Iterable<CardInfo> findCards(String text, int maxCount);

    CardInfo getCardById(String cardId);

    View getCardDetailsScreen(Context context, String cardId);

    interface ProgressUpdate {
        void updateProgress(int count, int max);
    }

    interface CancellableUpdate {
        void cancel();
    }

    interface UpdateResult {
        void error(String errorMessage);

        void success();

        void cancelled();
    }
}
