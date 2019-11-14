package com.powsybl.cgmes.conversion.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChange {

    public IidmChange(Identifiable identifiable) {
        this.identifiable = Objects.requireNonNull(identifiable);
    }

    public Identifiable getIdentifiable() {
        return identifiable;
    }

    // Should this change be explicitly copied to change log of target variant?
    public boolean shouldBeCopiedToTargetVariant() {
        return false;
    }

    private final Identifiable identifiable;
}
