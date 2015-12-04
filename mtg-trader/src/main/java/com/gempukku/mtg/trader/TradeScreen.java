package com.gempukku.mtg.trader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.dao.CardWithCountAndMultiplier;
import com.gempukku.mtg.trader.dao.TradeInfo;
import com.gempukku.mtg.trader.service.CardProvider;
import com.gempukku.mtg.trader.service.TradeStorage;
import com.gempukku.mtg.trader.ui.CardWithPriceListAdapter;
import com.gempukku.mtg.trader.ui.TradeModifierCallback;

public class TradeScreen extends AppCompatActivity {
    private static int ADD_THEIR_CARD_CODE = 1;
    private static int ADD_MINE_CARD_CODE = 2;

    private int _mineValue;
    private int _theirValue;
    private CardWithPriceListAdapter _mineCardList;
    private CardWithPriceListAdapter _theirCardList;

    private CardProvider _cardProvider;
    private TradeStorage _tradeStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_screen);

        MtgTraderApplication mtgTraderApplication = (MtgTraderApplication) getApplication();
        _cardProvider = mtgTraderApplication.getCardProvider();
        _tradeStorage = mtgTraderApplication.getTradeStorage();

        updateDatabaseStateUI();

        ListView myCardsView = (ListView) findViewById(R.id.myCards);
        _mineCardList = new CardWithPriceListAdapter(
                new TradeModifierCallback() {
                    @Override
                    public void tradeChanged() {
                        updateMinePrice();
                    }
                }, this, R.layout.trade_list_layout);
        myCardsView.setAdapter(_mineCardList);

        ListView theirCardsView = (ListView) findViewById(R.id.theirCards);
        _theirCardList = new CardWithPriceListAdapter(
                new TradeModifierCallback() {
                    @Override
                    public void tradeChanged() {
                        updateTheirPrice();
                    }
                }, this, R.layout.trade_list_layout);
        theirCardsView.setAdapter(_theirCardList);

        ImageButton addTheirButton = (ImageButton) findViewById(R.id.addTheir);
        addTheirButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TradeScreen.this, ChooseCardScreen.class);
                        startActivityForResult(intent, ADD_THEIR_CARD_CODE);
                    }
                });

        ImageButton addMineButton = (ImageButton) findViewById(R.id.addMine);
        addMineButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TradeScreen.this, ChooseCardScreen.class);
                        startActivityForResult(intent, ADD_MINE_CARD_CODE);
                    }
                });

        Button button = (Button) findViewById(R.id.storeTrade);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (_mineCardList.getCount() == 0 && _theirCardList.getCount() == 0) {
                            Toast toast = Toast.makeText(TradeScreen.this, getResources().getText(R.string.no_trade_to_store_toast), Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            TradeInfo tradeInfo = new TradeInfo(System.currentTimeMillis());
                            int cardCountMine = _mineCardList.getCount();
                            for (int i = 0; i < cardCountMine; i++) {
                                CardWithCountAndMultiplier myCard = _mineCardList.getItem(i);
                                CardInfo cardInfo = myCard.getCardInfo();
                                String cardId = cardInfo.getId();
                                tradeInfo.addMineCard(cardId, myCard.getCount(), cardInfo.getPrice(), myCard.getMultiplier());
                            }
                            int cardCountTheir = _theirCardList.getCount();
                            for (int i = 0; i < cardCountTheir; i++) {
                                CardWithCountAndMultiplier theirCard = _theirCardList.getItem(i);
                                CardInfo cardInfo = theirCard.getCardInfo();
                                String cardId = cardInfo.getId();
                                tradeInfo.addTheirCard(cardId, theirCard.getCount(), cardInfo.getPrice(), theirCard.getMultiplier());
                            }
                            _tradeStorage.storeTrade(tradeInfo);

                            _mineCardList.clear();
                            _theirCardList.clear();

                            updateMinePrice();
                            updateTheirPrice();

                            Toast toast = Toast.makeText(TradeScreen.this, getResources().getText(R.string.trade_stored_toast), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });

        updateMinePrice();
        updateTheirPrice();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnPaidNetwork() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null
                && (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                || activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE_DUN);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle cardsBundle = new Bundle();

        int myCardsCount = _mineCardList.getCount();
        for (int i = 0; i < myCardsCount; i++) {
            Bundle cardBundle = convertToCardBundle(_mineCardList.getItem(i));
            cardsBundle.putBundle("myCard" + i, cardBundle);
        }

        int theirCardsCount = _theirCardList.getCount();
        for (int i = 0; i < theirCardsCount; i++) {
            Bundle cardBundle = convertToCardBundle(_theirCardList.getItem(i));
            cardsBundle.putBundle("theirCard" + i, cardBundle);
        }

        outState.putBundle("cards", cardsBundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Bundle cardsBundle = savedInstanceState.getBundle("cards");
        int index = 0;
        while (true) {
            Bundle cardBundle = cardsBundle.getBundle("myCard" + index);
            if (cardBundle == null)
                break;
            convertToCardAndAddToList(cardBundle, _mineCardList);
            index++;
        }
        index = 0;
        while (true) {
            Bundle cardBundle = cardsBundle.getBundle("theirCard" + index);
            if (cardBundle == null)
                break;
            convertToCardAndAddToList(cardBundle, _theirCardList);
            index++;
        }

        updateMinePrice();
        updateTheirPrice();
    }

    private void convertToCardAndAddToList(Bundle cardBundle, CardWithPriceListAdapter list) {
        list.addCard(MtgTraderApplication.getCardInfo(cardBundle.getString("cardId"), _cardProvider),
                cardBundle.getInt("count"), cardBundle.getFloat("multiplier"));
    }

    private Bundle convertToCardBundle(CardWithCountAndMultiplier item) {
        Bundle cardBundle = new Bundle();
        cardBundle.putString("cardId", item.getCardInfo().getId());
        cardBundle.putInt("count", item.getCount());
        cardBundle.putFloat("multiplier", item.getMultiplier());
        return cardBundle;
    }

    private int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    private void updateDatabaseStateUI() {
        if (_cardProvider.isDatabaseOutdated()) {
            TextView outdatedAlert = (TextView) findViewById(R.id.outdatedAlert);
            String text = getOutdatedAlertText();
            outdatedAlert.setText(text);
            outdatedAlert.setVisibility(View.VISIBLE);
            findViewById(R.id.updatedInformation).setVisibility(View.GONE);
        } else {
            findViewById(R.id.outdatedAlert).setVisibility(View.GONE);
            TextView updatedInformation = (TextView) findViewById(R.id.updatedInformation);
            updatedInformation.setText(getUpdatedDatabaseText());
            updatedInformation.setVisibility(View.VISIBLE);
        }
    }

    public String getOutdatedAlertText() {
        boolean noDatabase = (_cardProvider.getDatabaseUpdateDate() == 0);
        if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            if (noDatabase) {
                return getResources().getString(R.string.no_database_label_landscape);
            } else {
                return getResources().getString(R.string.outdated_database_label_landscape);
            }
        } else {
            if (noDatabase) {
                return getResources().getString(R.string.no_database_label);
            } else {
                return getResources().getString(R.string.outdated_database_label);
            }
        }
    }

    private String getUpdatedDatabaseText() {
        String databaseUpdatedPrefix = getResources().getString(R.string.database_updated_prefix);
        if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            return databaseUpdatedPrefix + "\n" + MtgTraderApplication.formatDate(this, _cardProvider.getDatabaseUpdateDate());
        } else
            return databaseUpdatedPrefix + " " + MtgTraderApplication.formatDate(this, _cardProvider.getDatabaseUpdateDate());
    }

    private void updateMinePrice() {
        int cardCount = _mineCardList.getCount();
        _mineValue = 0;
        for (int i = 0; i < cardCount; i++) {
            CardWithCountAndMultiplier item = _mineCardList.getItem(i);
            _mineValue += MtgTraderApplication.evaluatePrice(item);
        }
        TextView myValue = (TextView) findViewById(R.id.myValue);
        myValue.setText(MtgTraderApplication.formatPrice(_mineValue));

        updatePriceResult();
    }

    private void updateTheirPrice() {
        int cardCount = _theirCardList.getCount();
        _theirValue = 0;
        for (int i = 0; i < cardCount; i++) {
            CardWithCountAndMultiplier item = _theirCardList.getItem(i);
            _theirValue += MtgTraderApplication.evaluatePrice(item);
        }
        TextView theirValue = (TextView) findViewById(R.id.theirValue);
        theirValue.setText(MtgTraderApplication.formatPrice(_theirValue));

        updatePriceResult();
    }

    public void addMineCard(CardInfo cardInfo, int count) {
        _mineCardList.addCard(cardInfo, count, 1f);
    }

    public void addTheirCard(CardInfo cardInfo, int count) {
        _theirCardList.addCard(cardInfo, count, 1f);
    }

    private void updatePriceResult() {
        TextView priceResult = (TextView) findViewById(R.id.priceResult);
        if (_mineValue > _theirValue) {
            priceResult.setText("Loss of " + MtgTraderApplication.formatPrice(_mineValue - _theirValue));
        } else if (_mineValue < _theirValue) {
            priceResult.setText("Gain of " + MtgTraderApplication.formatPrice(_theirValue - _mineValue));
        } else {
            priceResult.setText("Even trade");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trade, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update_database) {
            updateDatabase();
            return true;
        } else if (id == R.id.action_trade_history) {
            Intent intent = new Intent(this, HistoryScreen.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateDatabase() {
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(this, "There is no network connection available", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (isOnPaidNetwork()) {
                new AlertDialog.Builder(this)
                        .setTitle("Paid network")
                        .setMessage("Updating database on your network might incur heavy cost, are you sure you want to " +
                                "continue?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                doDatabaseUpdate();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            } else {
                doDatabaseUpdate();
            }
        }
    }

    private void doDatabaseUpdate() {
        final ProgressDialog updateProgressDialog = ProgressDialog.show(this, "Updating database", "Downloading database...", false, true);
        final CardProvider.CancellableUpdate cancellableUpdate = _cardProvider.updateDatabase(
                new CardProvider.ProgressUpdate() {
                    @Override
                    public void updateProgress(final int count, final int max) {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        updateProgressDialog.setMax(max);
                                        updateProgressDialog.setProgress(count);
                                    }
                                }
                        );
                    }
                },
                new CardProvider.UpdateResult() {
                    @Override
                    public void cancelled() {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        updateProgressDialog.dismiss();
                                    }
                                });
                    }

                    @Override
                    public void error(final String errorMessage) {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        updateProgressDialog.dismiss();
                                        Toast toast = Toast.makeText(TradeScreen.this, errorMessage, Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                    }

                    @Override
                    public void success() {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        updateProgressDialog.dismiss();
                                        updateDatabaseStateUI();
                                    }
                                });
                    }
                });
        updateProgressDialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancellableUpdate.cancel();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String cardId = data.getStringExtra("selectedCard");
            CardInfo cardInfo = MtgTraderApplication.getCardInfo(cardId, _cardProvider);
            if (requestCode == ADD_THEIR_CARD_CODE) {
                addTheirCard(cardInfo, 1);
                updateTheirPrice();
            } else if (requestCode == ADD_MINE_CARD_CODE) {
                addMineCard(cardInfo, 1);
                updateMinePrice();
            }
        }
    }
}
