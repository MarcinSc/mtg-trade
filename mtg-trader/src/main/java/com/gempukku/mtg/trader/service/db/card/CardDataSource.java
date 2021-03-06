package com.gempukku.mtg.trader.service.db.card;

import com.gempukku.mtg.trader.service.CardProvider;

public interface CardDataSource {
    CardProvider.CancellableUpdate updateInBackground(CardStorage cardStorage);

    String getSourceId();

    String getDisplayName();

    interface CardStorage {
        void startStoring(int max);

        void storeCard(DbCardInfo cardInfo);

        void finishStoring();

        void cancelled();

        void error(String message);
    }
}
