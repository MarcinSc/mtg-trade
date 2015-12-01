package com.gempukku.mtg.trader.dao;

public class CardWithCountAndMultiplier {
    private CardInfo _cardInfo;
    private int _count;
    private float _multiplier;

    public CardWithCountAndMultiplier(CardInfo cardInfo, int count, float multiplier) {
        _cardInfo = cardInfo;
        _count = count;
        _multiplier = multiplier;
    }

    public void setCount(int count) {
        _count = count;
    }

    public void setMultiplier(float multiplier) {
        _multiplier = multiplier;
    }

    public CardInfo getCardInfo() {
        return _cardInfo;
    }

    public int getCount() {
        return _count;
    }

    public float getMultiplier() {
        return _multiplier;
    }
}
