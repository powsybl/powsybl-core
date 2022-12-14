package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.BusTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

public class BusContingency extends AbstractContingency {

    public BusContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BUS;
    }

    @Override
    public Tripping toModification() {
        return new BusTripping(id);
    }

}
