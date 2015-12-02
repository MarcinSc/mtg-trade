package com.gempukku.mtg.trader.service.db.card;

import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;

public interface CardDataSource {
    CardProvider.CancellableUpdate updateInBackground(CardProvider.ProgressUpdate progressUpdate, CardStorage cardStorage, CardProvider.UpdateResult finishCallback);

    interface CardStorage {
        void startStoring();

        void storeCard(CardInfo cardInfo);

        void finishStoring(boolean success);
    }
}
