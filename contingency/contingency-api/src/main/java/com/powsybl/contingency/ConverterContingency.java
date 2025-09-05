package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.ConverterTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

public class ConverterContingency extends AbstractContingency {

    public ConverterContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.CONVERTER;
    }

    @Override
    public Tripping toModification() {
        return new ConverterTripping(id);
    }

}
