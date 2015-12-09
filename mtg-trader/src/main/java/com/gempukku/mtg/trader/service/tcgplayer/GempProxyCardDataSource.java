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
            boolean finishedWithoutError = false;
            try {
                URL url = new URL("http://www.gempukku.com/mtg-cards/database.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                try {

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        _cardStorage.error("Error communicating with the server, try again later");
                        return;
                    }

                    // download the file
                    InputStream input = connection.getInputStream();
                    try {
                        JsonReader reader = new JsonReader(new InputStreamReader(input, "UTF-8"));
                        try {
                            _cardStorage.startStoring(Integer.MAX_VALUE);
                            readSetArray(reader);

                            finishedWithoutError = true;
                            if (!_cancelled) {
                                // Got through without errors and was not cancelled
                                _cardStorage.finishStoring();
                            } else {
                                // Got through without errors, but was cancelled
                                _cardStorage.cancelled();
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
                _cardStorage.error("Error while communicating with the server, try again later");
            } finally {
                if (!finishedWithoutError) {
                    _cardStorage.error("Unknown error occured, try again later");
                }
            }
        }

        private void readSetArray(JsonReader reader) throws IOException {
            reader.beginArray();
            while (reader.hasNext() && !_cancelled) {
                reader.beginObject();
                // First attribute should be "name"
                String fieldName = reader.nextName();
                String info = reader.nextString();

                // Second attribute should be "cards"
                String nextFieldName = reader.nextName();
                readCardArray(reader, info);
                reader.endObject();
            }
            reader.endArray();
        }

        private void readCardArray(JsonReader reader, String info) throws IOException {
            reader.beginArray();
            while (reader.hasNext() && !_cancelled) {
                CardInfo cardInfo = readCard(reader, info);
                _cardStorage.storeCard(cardInfo);
            }
            reader.endArray();
        }

        private CardInfo readCard(JsonReader reader, String info) throws IOException {
            String id = null;
            String name = null;
            int price = 0;

            reader.beginObject();
            while (reader.hasNext()) {
                String fieldName = reader.nextName();
                if (fieldName.equals("id")) {
                    id = reader.nextString();
                } else if (fieldName.equals("name")) {
                    name = reader.nextString();
                } else if (fieldName.equals("price")) {
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
