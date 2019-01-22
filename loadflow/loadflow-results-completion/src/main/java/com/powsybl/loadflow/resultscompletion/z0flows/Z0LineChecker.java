package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;

public interface Z0LineChecker {

    public boolean isZ0(Line line, Bus b1, Bus b2);
}
