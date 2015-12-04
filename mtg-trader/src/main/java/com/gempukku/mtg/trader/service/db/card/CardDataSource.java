package com.gempukku.mtg.trader.service.db.card;

import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;

public interface CardDataSource {
    CardProvider.CancellableUpdate updateInBackground(CardStorage cardStorage);

    interface CardStorage {
        void startStoring(int max);

        void storeCard(CardInfo cardInfo);

        void finishStoring();

        void cancelled();

        void error(String message);
    }
}
