package com.powsybl.loadflow.validation.data;

import java.util.Objects;

public record BusData(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP,
               double genQ, double batP, double batQ, double shuntP, double shuntQ, double svcP,
               double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
               double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP,
               double tltQ, boolean mainComponent, boolean validated) {
    public BusData {
        Objects.requireNonNull(busId);
    }
}
