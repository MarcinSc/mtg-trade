package com.gempukku.mtg.trader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import com.gempukku.mtg.trader.service.TradeStorage;
import com.gempukku.mtg.trader.ui.TradeHistoryListAdapter;

public class HistoryScreen extends AppCompatActivity {
    private TradeStorage _tradeStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_screen);

        MtgTraderApplication application = (MtgTraderApplication) getApplication();
        _tradeStorage = application.getTradeStorage();

        ListView tradesList = (ListView) findViewById(R.id.tradesList);

        TradeHistoryListAdapter adapter = new TradeHistoryListAdapter(application.getCardProvider(), this, R.layout.history_list_layout, _tradeStorage.listTrades());
        tradesList.setAdapter(adapter);

        TextView historicalProfit = (TextView) findViewById(R.id.historicalProfit);
        historicalProfit.setText("Historical profit: " + MtgTraderApplication.formatPrice(_tradeStorage.getHistoricalProfit()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
