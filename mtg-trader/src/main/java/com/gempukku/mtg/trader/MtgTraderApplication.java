package com.gempukku.mtg.trader;

import android.app.Application;
import android.content.Context;
import android.text.format.DateFormat;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.dao.CardWithCountAndMultiplier;
import com.gempukku.mtg.trader.service.CardProvider;
import com.gempukku.mtg.trader.service.TradeStorage;
import com.gempukku.mtg.trader.service.db.card.DbCardProvider;
import com.gempukku.mtg.trader.service.db.trade.DbTradeStorage;
import com.gempukku.mtg.trader.service.proxy.GempProxyCardDataSource;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class MtgTraderApplication extends Application {
    private CardProvider _cardProvider;
    private TradeStorage _tradeStorage;

    public MtgTraderApplication() {
        GempProxyCardDataSource cardDataSource = new GempProxyCardDataSource("mtgGoldFish", "MtgGoldFish.com");

        _cardProvider = new DbCardProvider(this, cardDataSource);

        _tradeStorage = new DbTradeStorage(this);
    }

    public CardProvider getCardProvider() {
        return _cardProvider;
    }

    public TradeStorage getTradeStorage() {
        return _tradeStorage;
    }

    public static CardInfo getCardInfo(String cardId, CardProvider cardProvider) {
        if (CardInfo.isCash(cardId)) {
            return new CardInfo(cardId, "Cash", "", CardInfo.getCashValue(cardId));
        } else {
            return cardProvider.getCardById(cardId);
        }
    }

    public static String formatPrice(int cardPrice) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(cardPrice / 100f);
    }

    public static String formatCardCount(int count, CardInfo cardInfo) {
        if (CardInfo.isCash(cardInfo)) {
            return "Cash";
        } else {
            if (cardInfo != null) {
                return count + "x " + cardInfo.getName();
            } else {
                return count + "x Unknown card";
            }
        }
    }

    public static int evaluatePrice(CardWithCountAndMultiplier card) {
        CardInfo cardInfo = card.getCardInfo();
        int count = card.getCount();
        return Math.round(count * cardInfo.getPrice() * card.getMultiplier());
    }

    public static String formatDate(Context context, long date) {
        Date tradeDate = new Date(date);
        String dateStr = DateFormat.getDateFormat(context).format(tradeDate);
        String timeStr = DateFormat.getTimeFormat(context).format(tradeDate);
        return dateStr + " " + timeStr;
    }
}
