package com.powsybl.dynamicsimulation;

import java.util.Collections;
import java.util.List;

import com.powsybl.iidm.network.Network;

public class DynamicModelsSupplierMock implements DynamicModelsSupplier {

    static DynamicModelsSupplier empty() {
        return network -> Collections.emptyList();
    }

    @Override
    public List<DynamicModel> get(Network network) {
        return null;
    }

}
