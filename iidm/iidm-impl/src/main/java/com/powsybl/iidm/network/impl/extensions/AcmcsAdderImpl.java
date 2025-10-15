package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Acmc;
import com.powsybl.iidm.network.extensions.AcmcAdder;
import com.powsybl.iidm.network.extensions.Acmcs;
import com.powsybl.iidm.network.extensions.AcmcsAdder;
import com.powsybl.iidm.network.impl.NetworkImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class AcmcsAdderImpl extends AbstractIidmExtensionAdder<Network, Acmcs> implements AcmcsAdder {

    private final List<Acmc> acmcs = new ArrayList<>();

    public AcmcsAdderImpl(Network network) {
        super(network);
    }

    @Override
    public Class<? super Acmcs> getExtensionClass() {
        return Acmcs.class;
    }

    void addAcmc(AcmcImpl acmc) {
        acmcs.add(Objects.requireNonNull(acmc));
    }

    @Override
    public AcmcAdder newAcmc() {
        return new AcmcAdderImpl(this);
    }

    @Override
    protected AcmcsImpl createExtension(Network network) {
        if (acmcs.isEmpty()) {
            throw new PowsyblException("Empty control zone list");
        }
        var acmcsVar = new AcmcsImpl(acmcs);
        for (var acmc : acmcs) {
            ((AcmcImpl) acmc).setAcmcs(acmcsVar);
        }
        return acmcsVar;
    }

    NetworkImpl getNetwork() {
        return (NetworkImpl) extendable;
    }
}
