package com.gempukku.mtg.trader.service.temp;

import com.gempukku.mtg.trader.dao.TradeEntry;
import com.gempukku.mtg.trader.dao.TradeInfo;
import com.gempukku.mtg.trader.service.TradeStorage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MemoryTradeStorage implements TradeStorage {
    private List<TradeInfo> _tradeHistory = new LinkedList<TradeInfo>();

    @Override
    public List<TradeInfo> listTrades() {
        return Collections.unmodifiableList(_tradeHistory);
    }

    @Override
    public void storeTrade(TradeInfo trade) {
        _tradeHistory.add(0, trade);
    }

    @Override
    public int getHistoricalProfit() {
        int result = 0;
        for (TradeInfo tradeInfo : _tradeHistory) {
            for (TradeEntry myCard : tradeInfo.getMyCards()) {
                result -= Math.round(myCard.getPrice() * myCard.getCount() * myCard.getMultiplier());
            }
            for (TradeEntry theirCard : tradeInfo.getTheirCards()) {
                result -= Math.round(theirCard.getPrice() * theirCard.getCount() * theirCard.getMultiplier());
            }
        }
        return result;
    }
}
