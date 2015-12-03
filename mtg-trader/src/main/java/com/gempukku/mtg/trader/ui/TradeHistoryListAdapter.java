package com.gempukku.mtg.trader.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.gempukku.mtg.trader.MtgTraderApplication;
import com.gempukku.mtg.trader.R;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.dao.CardValueMultiplier;
import com.gempukku.mtg.trader.dao.TradeEntry;
import com.gempukku.mtg.trader.dao.TradeInfo;
import com.gempukku.mtg.trader.service.CardProvider;

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

        Context context = getContext();
        LayoutInflater vi = LayoutInflater.from(context);

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
            textView.setText(MtgTraderApplication.formatDate(getContext(), tradeInfo.getDate()));

            int profit = 0;

            for (TradeEntry tradeEntry : tradeInfo.getTheirCards()) {
                profit += appendCardLayoutToView(vi, theirCards, tradeEntry);
            }

            for (TradeEntry tradeEntry : tradeInfo.getMyCards()) {
                profit -= appendCardLayoutToView(vi, myCards, tradeEntry);
            }

            TextView tradeProfit = (TextView) v.findViewById(R.id.tradeProfit);
            tradeProfit.setText("Profit: " + MtgTraderApplication.formatPrice(profit));
            int lossColor = context.getResources().getColor(R.color.history_label_loss);
            int profitColor = context.getResources().getColor(R.color.history_label_profit);
            tradeProfit.setTextColor((profit < 0) ? lossColor : profitColor);
        }

        return v;
    }

    private int appendCardLayoutToView(LayoutInflater vi, ViewGroup theirCards, TradeEntry tradeEntry) {
        View separator = vi.inflate(R.layout.history_list_separator_fragment, null);
        theirCards.addView(separator);

        String cardId = tradeEntry.getCardId();
        int count = tradeEntry.getCount();
        int price = tradeEntry.getPrice();
        float multiplier = tradeEntry.getMultiplier();

        CardInfo cardInfo = MtgTraderApplication.getCardInfo(cardId, _cardProvider);
        String versionInfo = getVersionInfo(cardInfo);

        int priceTotal = Math.round(price * count * multiplier);

        View cardView = vi.inflate(R.layout.history_single_card_fragment, null);
        ((TextView) cardView.findViewById(R.id.name)).setText(MtgTraderApplication.formatCardCount(count, cardInfo));
        ((TextView) cardView.findViewById(R.id.info)).setText(versionInfo);
        ((TextView) cardView.findViewById(R.id.price)).setText(MtgTraderApplication.formatPrice(priceTotal));

        TextView multiplierText = ((TextView) cardView.findViewById(R.id.multiplier));
        if (CardInfo.isCash(cardInfo)) {
            multiplierText.setText("");
        } else {
            multiplierText.setText(CardValueMultiplier.getClosest(multiplier).getDisplayValue());
        }
        theirCards.addView(cardView);
        return priceTotal;
    }

    private String getVersionInfo(CardInfo cardInfo) {
        String versionInfo;
        if (cardInfo != null)
            versionInfo = cardInfo.getVersionInfo();
        else
            versionInfo = "";
        return versionInfo;
    }
}
