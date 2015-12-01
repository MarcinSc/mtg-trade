package com.gempukku.mtg.trader.dao;

public class TradeEntry {
    private String _cardId;
    private int _count;
    private int _price;
    private float _multiplier;

    public TradeEntry(String cardId, int count, int price, float multiplier) {
        _cardId = cardId;
        _count = count;
        _price = price;
        _multiplier = multiplier;
    }

    public String getCardId() {
        return _cardId;
    }

    public int getCount() {
        return _count;
    }

    public float getMultiplier() {
        return _multiplier;
    }

    public int getPrice() {
        return _price;
    }
}
