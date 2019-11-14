package com.powsybl.cgmes.conversion.update;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangeRemoval extends IidmChange {

    public IidmChangeRemoval(Identifiable<?> identifiable) {
        super(identifiable);
    }

}
