package com.powsybl.dynamicsimulation;

import java.util.Collections;
import java.util.List;

import com.powsybl.iidm.network.Network;

public class DynamicModelSupplierMock implements DynamicModelSupplier {

    static DynamicModelSupplier empty() {
        return network -> Collections.emptyList();
    }

    @Override
    public List<DynamicModel> get(Network network) {
        return null;
    }

}
