package com.gempukku.mtg.trader.dao;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TradeInfo {
    private long _date;
    private String _providerId;
    private String _providerName;
    private List<TradeEntry> _myCards = new LinkedList<TradeEntry>();
    private List<TradeEntry> _theirCards = new LinkedList<TradeEntry>();

    public TradeInfo(long date, String providerId, String providerName) {
        _date = date;
        _providerId = providerId;
        _providerName = providerName;
    }

    public void addMineCard(String cardId, int count, int price, float multiplier) {
        _myCards.add(new TradeEntry(cardId, count, price, multiplier));
    }

    public void addTheirCard(String cardId, int count, int price, float multiplier) {
        _theirCards.add(new TradeEntry(cardId, count, price, multiplier));
    }

    public Iterable<TradeEntry> getMyCards() {
        return Collections.unmodifiableList(_myCards);
    }

    public Iterable<TradeEntry> getTheirCards() {
        return Collections.unmodifiableList(_theirCards);
    }

    public long getDate() {
        return _date;
    }

    public String getProviderId() {
        return _providerId;
    }

    public String getProviderName() {
        return _providerName;
    }
}
