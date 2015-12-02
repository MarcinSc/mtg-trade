package com.gempukku.mtg.trader.service.tcgplayer;

import android.util.JsonReader;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;
import com.gempukku.mtg.trader.service.db.card.CardDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GempProxyCardDataSource implements CardDataSource {
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
            boolean startedStoring = false;
            boolean finishedStoring = false;
            try {
                URL url = new URL("http://www.gempukku.com/mtg-cards/database.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                try {

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        _finishCallback.error("Error communicating with the server, try again later");
                        return;
                    }

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input = connection.getInputStream();
                    try {
                        JsonReader reader = new JsonReader(new InputStreamReader(input, "UTF-8"));
                        try {
                            startedStoring = true;
                            _cardStorage.startStoring();
                            readCardArray(reader);
                            if (!_cancelled) {
                                // Got through without errors and was not cancelled
                                finishedStoring = true;
                                _cardStorage.finishStoring(true);
                                _finishCallback.success();
                            } else {
                                // Got through without errors, but was cancelled
                                finishedStoring = true;
                                _cardStorage.finishStoring(false);
                                _finishCallback.cancelled();
                            }
                        } finally {
                            reader.close();
                        }
                    } finally {
                        input.close();
                    }
                } finally {
                    connection.disconnect();
                }

            } catch (IOException exp) {
                if (startedStoring) {
                    // Error in communicating
                    finishedStoring = true;
                    _cardStorage.finishStoring(false);
                    _finishCallback.error("Error while communicating with the server, try again later");
                }
            } finally {
                if (startedStoring && !finishedStoring) {
                    // Some other error and started storing
                    _cardStorage.finishStoring(false);
                    _finishCallback.error("Unknown error occured, try again later");
                }
                if (!startedStoring) {
                    // Some other error and has not even started storing
                    _finishCallback.error("Unknown error occured, try again later");
                }
            }
        }

        private void readCardArray(JsonReader reader) throws IOException {
            reader.beginArray();
            while (reader.hasNext() && !_cancelled) {
                CardInfo cardInfo = readCard(reader);
                _cardStorage.storeCard(cardInfo);
            }
            reader.endArray();
        }

        private CardInfo readCard(JsonReader reader) throws IOException {
            String id = null;
            String name = null;
            String info = null;
            int price = 0;

            reader.beginObject();
            while (reader.hasNext()) {
                String fieldName = reader.nextName();
                if (fieldName.equals("id")) {
                    id = reader.nextString();
                } else if (fieldName.equals("name")) {
                    name = reader.nextString();
                } else if (fieldName.equals("info")) {
                    info = reader.nextString();
                } else if (fieldName.equals("midPrice")) {
                    price = reader.nextInt();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return new CardInfo(id, name, info, price);
        }

        public void cancel() {
            _cancelled = true;
        }
    }
}
