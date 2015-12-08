package com.gempukku.mtg.trader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.gempukku.mtg.trader.service.CardProvider;

public class CardDetails extends AppCompatActivity {

    public static final String CARD_ID_KEY = "cardId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        Intent intent = getIntent();
        String cardId = intent.getStringExtra(CARD_ID_KEY);

        MtgTraderApplication mtgTraderApplication = (MtgTraderApplication) getApplication();
        CardProvider cardProvider = mtgTraderApplication.getCardProvider();
        View cardDetailsScreen = cardProvider.getCardDetailsScreen(this, cardId);

        ViewGroup rootView = (ViewGroup) findViewById(R.id.rootView);
        rootView.addView(cardDetailsScreen, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card_details, menu);
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
