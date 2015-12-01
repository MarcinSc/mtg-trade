package com.gempukku.mtg.trader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    private static long OUTDATED_ALERT_THRESHOLD = 3 * 24 * 60 * 60 * 1000; // 3 days
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

        if (_cardProvider.getDatabaseUpdateDate() + OUTDATED_ALERT_THRESHOLD > System.currentTimeMillis()) {
            findViewById(R.id.outdatedAlert).setVisibility(View.GONE);
        }

        ListView myCardsView = (ListView) findViewById(R.id.myCards);
        _mineCardList = new CardWithPriceListAdapter(
                new TradeModifierCallback() {
                    @Override
                    public void tradeChanged() {
                        updateMinePrice();
                    }
                }, getApplicationContext(), R.layout.trade_list_layout);
        myCardsView.setAdapter(_mineCardList);

        ListView theirCardsView = (ListView) findViewById(R.id.theirCards);
        _theirCardList = new CardWithPriceListAdapter(
                new TradeModifierCallback() {
                    @Override
                    public void tradeChanged() {
                        updateTheirPrice();
                    }
                }, getApplicationContext(), R.layout.trade_list_layout);
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
                            Toast toast = Toast.makeText(getApplicationContext(), getResources().getText(R.string.no_trade_to_store_toast), Toast.LENGTH_SHORT);
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

                            Toast toast = Toast.makeText(getApplicationContext(), getResources().getText(R.string.trade_stored_toast), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });

        updateMinePrice();
        updateTheirPrice();
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
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_trade_history) {
            Intent intent = new Intent(this, HistoryScreen.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
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
