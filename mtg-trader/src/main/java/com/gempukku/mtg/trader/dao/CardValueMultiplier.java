package com.gempukku.mtg.trader.dao;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardValueMultiplier {
    public static final List<CardValueMultiplier> MULTIPLIERS;

    static {
        List<CardValueMultiplier> multipliers = new ArrayList<CardValueMultiplier>();
        DecimalFormat format = new DecimalFormat("+#'%';-#'%'");

        for (int i = -95; i < 50; i += 5) {
            multipliers.add(new CardValueMultiplier(1 + 0.01f * i, format.format(i)));
        }
        for (int i = 60; i < 200; i += 10) {
            multipliers.add(new CardValueMultiplier(1 + 0.01f * i, format.format(i)));
        }
        for (int i = 200; i <= 300; i += 20) {
            multipliers.add(new CardValueMultiplier(1 + 0.01f * i, format.format(i)));
        }
        MULTIPLIERS = Collections.unmodifiableList(multipliers);
    }

    public static CardValueMultiplier getClosest(float multiplier) {
        float bestDif = Float.MAX_VALUE;
        CardValueMultiplier bestFit = null;
        for (CardValueMultiplier cardValueMultiplier : MULTIPLIERS) {
            float absDif = Math.abs(cardValueMultiplier.getMultiplier() - multiplier);
            if (absDif < bestDif) {
                bestDif = absDif;
                bestFit = cardValueMultiplier;
            }
        }
        return bestFit;
    }

    public static int getClosestIndex(float multiplier) {
        return MULTIPLIERS.indexOf(getClosest(multiplier));
    }

    private float _multiplier;
    private String _displayValue;

    private CardValueMultiplier(float multiplier, String displayValue) {
        _multiplier = multiplier;
        _displayValue = displayValue;
    }

    public float getMultiplier() {
        return _multiplier;
    }

    public String getDisplayValue() {
        return _displayValue;
    }
}
