package com.powsybl.cgmes.conversion.update;

import com.powsybl.iidm.network.Identifiable;

public class IidmChangeCreation extends IidmChange {

    public IidmChangeCreation(Identifiable<?> identifiable) {
        super(identifiable);
    }

}
