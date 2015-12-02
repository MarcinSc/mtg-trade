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

    public void addCard(CardInfo cardInfo) {
        _cards.add(cardInfo);
    }

    @Override
    public CardProvider.CancellableUpdate updateInBackground(CardProvider.ProgressUpdate progressUpdate, CardStorage cardStorage, CardProvider.UpdateResult finishCallback) {
        final UpdateRunnable updateRunnable = new UpdateRunnable(progressUpdate, cardStorage, finishCallback);
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
        private CardProvider.ProgressUpdate _progressUpdate;
        private CardStorage _cardStorage;
        private CardProvider.UpdateResult _finishCallback;

        private volatile boolean _cancelled;

        public UpdateRunnable(CardProvider.ProgressUpdate progressUpdate, CardStorage cardStorage, CardProvider.UpdateResult finishCallback) {
            _progressUpdate = progressUpdate;
            _cardStorage = cardStorage;
            _finishCallback = finishCallback;
        }

        @Override
        public void run() {
            int max = _cards.size();

            _cardStorage.startStoring();
            boolean calledFinish = false;
            try {
                _progressUpdate.updateProgress(0, max);
                int count = 0;
                for (CardInfo card : _cards) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException exp) {

                    }
                    if (_cancelled) {
                        calledFinish = true;
                        _cardStorage.finishStoring(false);
                        _finishCallback.cancelled();
                        break;
                    }

                    _cardStorage.storeCard(card);
                    count++;
                    _progressUpdate.updateProgress(count, max);
                }
                if (!_cancelled) {
                    calledFinish = true;
                    _cardStorage.finishStoring(true);
                    _finishCallback.success();
                }
            } finally {
                if (!calledFinish) {
                    _cardStorage.finishStoring(false);
                    _finishCallback.error("Unknown error occured");
                }
            }
        }

        public void cancel() {
            _cancelled = true;
        }
    }
}
