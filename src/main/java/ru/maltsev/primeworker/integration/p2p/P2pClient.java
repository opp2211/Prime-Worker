package ru.maltsev.primeworker.integration.p2p;

import ru.maltsev.primeworker.domain.p2p.P2pAd;
import ru.maltsev.primeworker.domain.p2p.P2pQuery;

import java.util.List;

public interface P2pClient {

    List<P2pAd> findAds(P2pQuery query);

    default P2pAd findAd(P2pQuery query, int position) {
        P2pQuery pagedQuery = new P2pQuery(
                query.asset(),
                query.fiat(),
                query.side(),
                query.paymentMethods(),
                query.amount(),
                position,
                1
        );
        List<P2pAd> ads = findAds(pagedQuery);
        if (ads.isEmpty()) {
            throw new IllegalStateException("No ads returned for position " + position);
        }
        return ads.getFirst();
    }
}
