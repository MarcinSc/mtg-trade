package com.gempukku.mtg.trader.service;

import com.gempukku.mtg.trader.dao.CardInfo;

public interface CardProvider {
    long getDatabaseUpdateDate();

    Iterable<CardInfo> findCards(String text, int maxCount);

    CardInfo getCardById(String cardId);
}
