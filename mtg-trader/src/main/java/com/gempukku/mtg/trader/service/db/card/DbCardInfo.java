package com.gempukku.mtg.trader.service.db.card;

import com.gempukku.mtg.trader.dao.CardInfo;

public class DbCardInfo extends CardInfo {
    private String _link;

    public DbCardInfo(String id, String name, String versionInfo, int price, String link) {
        super(id, name, versionInfo, price);
        _link = link;
    }

    public String getLink() {
        return _link;
    }
}
