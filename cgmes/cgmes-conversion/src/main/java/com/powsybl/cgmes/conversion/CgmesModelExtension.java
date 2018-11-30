package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

public class CgmesModelExtension implements Extension<Network> {

    public CgmesModelExtension(CgmesModel cgmes) {
        this.cgmes = cgmes;
    }

    public CgmesModel cgmes() {
        return cgmes;
    }

    @Override
    public String getName() {
        return "CgmesModel";
    }

    @Override
    public Network getExtendable() {
        return network;
    }

    @Override
    public void setExtendable(Network network) {
        this.network = network;

    }

    private Network network;
    private final CgmesModel cgmes;
}
