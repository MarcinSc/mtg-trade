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
import com.gempukku.mtg.trader.service.temp.MemoryCardDataSource;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class MtgTraderApplication extends Application {
    private CardProvider _cardProvider;
    private TradeStorage _tradeStorage;

    public MtgTraderApplication() {
        MemoryCardDataSource cardDataSource = new MemoryCardDataSource();
        cardDataSource.addCard(new CardInfo("sc", "Snapcaster Mage", "Innistrad", 5956));
        cardDataSource.addCard(new CardInfo("scf", "Snapcaster Mage", "Innistrad · Foil", 18040));
        cardDataSource.addCard(new CardInfo("ara", "Ancestral Recall", "Alpha", 500000));
        cardDataSource.addCard(new CardInfo("arb", "Ancestral Recall", "Beta", 300000));
        cardDataSource.addCard(new CardInfo("aru", "Ancestral Recall", "Unlimited", 50000));

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
            return count + "x " + cardInfo.getName();
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
