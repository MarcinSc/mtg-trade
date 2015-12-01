package com.gempukku.mtg.trader.service;

import com.gempukku.mtg.trader.dao.TradeInfo;

import java.util.List;

public interface TradeStorage {
    void storeTrade(TradeInfo trade);

    List<TradeInfo> listTrades();

    int getHistoricalProfit();
}
