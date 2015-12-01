package com.gempukku.mtg.trader.service;

import com.gempukku.mtg.trader.dao.CardInfo;

public interface CardProvider {
    long getDatabaseUpdateDate();

    boolean isDatabaseOutdated();

    CancellableUpdate updateDatabase(ProgressUpdate progressUpdate, Runnable finishCallback);

    Iterable<CardInfo> findCards(String text, int maxCount);

    CardInfo getCardById(String cardId);

    interface ProgressUpdate {
        void updateProgress(int count, int max);
    }

    interface CancellableUpdate {
        void cancel();
    }
}
