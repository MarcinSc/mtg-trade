package com.gempukku.mtg.trader.service.temp;

import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;
import com.gempukku.mtg.trader.service.db.card.CardDataSource;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class MemoryCardDataSource implements CardDataSource {
    private Set<CardInfo> _cards = new TreeSet<CardInfo>(
            new Comparator<CardInfo>() {
                @Override
                public int compare(CardInfo lhs, CardInfo rhs) {
                    int result = lhs.getName().compareTo(rhs.getName());
                    if (result == 0) {
                        result = lhs.getVersionInfo().compareTo(rhs.getVersionInfo());
                    }
                    return result;
                }
            });

    @Override
    public String getDisplayName() {
        return "Test Source";
    }

    @Override
    public String getSourceId() {
        return "memory";
    }

    public void addCard(CardInfo cardInfo) {
        _cards.add(cardInfo);
    }

    @Override
    public CardProvider.CancellableUpdate updateInBackground(CardStorage cardStorage) {
        final UpdateRunnable updateRunnable = new UpdateRunnable(cardStorage);
        Thread thr = new Thread(updateRunnable);
        thr.start();
        return new CardProvider.CancellableUpdate() {
            @Override
            public void cancel() {
                updateRunnable.cancel();
            }
        };
    }

    private class UpdateRunnable implements Runnable {
        private CardStorage _cardStorage;

        private volatile boolean _cancelled;

        public UpdateRunnable(CardStorage cardStorage) {
            _cardStorage = cardStorage;
        }

        @Override
        public void run() {
            int max = _cards.size();

            _cardStorage.startStoring(max);
            boolean finishedWithoutError = false;
            try {
                for (CardInfo card : _cards) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException exp) {

                    }
                    if (_cancelled) {
                        finishedWithoutError = true;
                        _cardStorage.cancelled();
                        break;
                    }

                    _cardStorage.storeCard(card);
                }
                if (!_cancelled) {
                    finishedWithoutError = true;
                    _cardStorage.finishStoring();
                }
            } finally {
                if (!finishedWithoutError) {
                    _cardStorage.error("Unknown error occured");
                }
            }
        }

        public void cancel() {
            _cancelled = true;
        }
    }
}
