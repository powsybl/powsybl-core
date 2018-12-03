package com.powsybl.cgmes.conversion;

import java.util.Objects;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

public class CgmesModelExtension extends AbstractExtension<Network> {

    public CgmesModelExtension(CgmesModel cgmes) {
        Objects.requireNonNull(cgmes);
        this.cgmes = cgmes;
    }

    public CgmesModel getCgmesModel() {
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
