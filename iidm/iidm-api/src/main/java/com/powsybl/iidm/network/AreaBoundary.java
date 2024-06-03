package com.powsybl.iidm.network;

import java.util.Optional;

public interface AreaBoundary {

    Optional<Terminal> getTerminal();

    Optional<DanglingLine> getDanglingLine();

    boolean isAc();

    double getP();

    double getQ();

}
