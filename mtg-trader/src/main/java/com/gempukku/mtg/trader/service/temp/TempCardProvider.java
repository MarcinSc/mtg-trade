package com.gempukku.mtg.trader.service.temp;

import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TempCardProvider implements CardProvider {
    private Set<CardInfo> _cards = new TreeSet<CardInfo>(
            new Comparator<CardInfo>() {
                @Override
                public int compare(CardInfo lhs, CardInfo rhs) {
                    int result = lhs.getName().compareTo(rhs.getName());
                    if (result == 0) {
                        result = lhs.getVersionInfo().compareTo(rhs.getVersionInfo());
                    }
                    return result;
                }
            });

    public void addCard(CardInfo cardInfo) {
        _cards.add(cardInfo);
    }

    @Override
    public Iterable<CardInfo> findCards(String text, int maxCards) {
        String lowerCaseText = text.toLowerCase();
        List<CardInfo> matches = new LinkedList<CardInfo>();
        if (text.isEmpty())
            return matches;
        int count = 0;
        for (CardInfo card : _cards) {
            if (card.getName().toLowerCase().contains(lowerCaseText)) {
                matches.add(card);
                count++;
            }
            if (count == maxCards) {
                break;
            }
        }

        return matches;
    }

    @Override
    public CardInfo getCardById(String cardId) {
        for (CardInfo card : _cards) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public long getDatabaseUpdateDate() {
        return System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
    }
}
