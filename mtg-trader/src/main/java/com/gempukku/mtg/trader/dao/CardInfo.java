package com.gempukku.mtg.trader.dao;

public class CardInfo {
    private static final String CASH_ID_PREFIX = "cash:";

    public static boolean isCash(CardInfo cardInfo) {
        return cardInfo != null && isCash(cardInfo.getId());
    }

    public static boolean isCash(String cardId) {
        return cardId.startsWith(CASH_ID_PREFIX);
    }

    public static String createCashCardId(int cash) {
        return CASH_ID_PREFIX + cash;
    }

    public static CardInfo createCash(int cash) {
        return new CardInfo(createCashCardId(cash), "Cash", "", cash);
    }

    public static int getCashValue(String cardId) {
        return Integer.parseInt(cardId.substring(CASH_ID_PREFIX.length()));
    }

    private String _id;
    private String _name;
    private String _versionInfo;
    private int _price;

    public CardInfo(String id, String name, String versionInfo, int price) {
        _id = id;
        _name = name;
        _versionInfo = versionInfo;
        _price = price;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getVersionInfo() {
        return _versionInfo;
    }

    public int getPrice() {
        return _price;
    }
}
