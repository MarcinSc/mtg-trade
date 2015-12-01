package com.gempukku.mtg.trader.ui;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.gempukku.mtg.trader.MtgTraderApplication;
import com.gempukku.mtg.trader.R;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.dao.TradeEntry;
import com.gempukku.mtg.trader.dao.TradeInfo;
import com.gempukku.mtg.trader.service.CardProvider;

import java.util.Date;
import java.util.List;

public class TradeHistoryListAdapter extends ArrayAdapter<TradeInfo> {
    private CardProvider _cardProvider;

    public TradeHistoryListAdapter(CardProvider cardProvider, Context context, int resource, List<TradeInfo> objects) {
        super(context, resource, objects);
        _cardProvider = cardProvider;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        LayoutInflater vi = LayoutInflater.from(getContext());

        if (v == null) {
            v = vi.inflate(R.layout.history_list_layout, null);
        }

        ViewGroup theirCards = (ViewGroup) v.findViewById(R.id.theirCards);
        ViewGroup myCards = (ViewGroup) v.findViewById(R.id.myCards);
        while (theirCards.getChildCount() > 1) {
            theirCards.removeViewAt(1);
        }
        while (myCards.getChildCount() > 1) {
            myCards.removeViewAt(1);
        }

        TradeInfo tradeInfo = getItem(position);

        if (tradeInfo != null) {
            TextView textView = (TextView) v.findViewById(R.id.tradeDate);
            Date tradeDate = new Date(tradeInfo.getDate());
            String dateStr = DateFormat.getDateFormat(getContext()).format(tradeDate);
            String timeStr = DateFormat.getTimeFormat(getContext()).format(tradeDate);
            textView.setText(dateStr + " " + timeStr);

            int profit = 0;

            for (TradeEntry tradeEntry : tradeInfo.getTheirCards()) {
                View separator = vi.inflate(R.layout.history_list_separator_fragment, null);
                theirCards.addView(separator);

                String cardId = tradeEntry.getCardId();
                int count = tradeEntry.getCount();
                int price = tradeEntry.getPrice();
                float multiplier = tradeEntry.getMultiplier();

                CardInfo cardInfo = MtgTraderApplication.getCardInfo(cardId, _cardProvider);

                int priceTotal = Math.round(price * count * multiplier);
                profit += priceTotal;

                View cardView = vi.inflate(R.layout.history_single_card_fragment, null);
                ((TextView) cardView.findViewById(R.id.name)).setText(MtgTraderApplication.formatCardCount(count, cardInfo));
                ((TextView) cardView.findViewById(R.id.info)).setText(cardInfo.getVersionInfo());
                ((TextView) cardView.findViewById(R.id.price)).setText(MtgTraderApplication.formatPrice(priceTotal));
                theirCards.addView(cardView);
            }

            for (TradeEntry tradeEntry : tradeInfo.getMyCards()) {
                View separator = vi.inflate(R.layout.history_list_separator_fragment, null);
                myCards.addView(separator);

                String cardId = tradeEntry.getCardId();
                int count = tradeEntry.getCount();
                int price = tradeEntry.getPrice();
                float multiplier = tradeEntry.getMultiplier();

                CardInfo cardInfo = MtgTraderApplication.getCardInfo(cardId, _cardProvider);

                int priceTotal = Math.round(price * count * multiplier);
                profit -= priceTotal;

                View cardView = vi.inflate(R.layout.history_single_card_fragment, null);
                ((TextView) cardView.findViewById(R.id.name)).setText(MtgTraderApplication.formatCardCount(count, cardInfo));
                ((TextView) cardView.findViewById(R.id.info)).setText(cardInfo.getVersionInfo());
                ((TextView) cardView.findViewById(R.id.price)).setText(MtgTraderApplication.formatPrice(priceTotal));
                myCards.addView(cardView);
            }

            TextView tradeProfit = (TextView) v.findViewById(R.id.tradeProfit);
            tradeProfit.setText("Profit: " + MtgTraderApplication.formatPrice(profit));
            int color = (profit < 0) ? Color.RED : Color.rgb(0, 0xaa, 0);
            tradeProfit.setTextColor(color);
        }

        return v;
    }
}
