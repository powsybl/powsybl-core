package com.powsybl.cgmes.update;

import java.util.Objects;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangeOnRemove {

    public IidmChangeOnRemove(Identifiable identifiable, String variant) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.variant = Objects.requireNonNull(variant);
    }

    private final Identifiable identifiable;
    private final String variant;
}
