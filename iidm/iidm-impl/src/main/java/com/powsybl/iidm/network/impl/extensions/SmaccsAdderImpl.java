package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Smacc;
import com.powsybl.iidm.network.extensions.SmaccAdder;
import com.powsybl.iidm.network.extensions.Smaccs;
import com.powsybl.iidm.network.extensions.SmaccsAdder;
import com.powsybl.iidm.network.impl.NetworkImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class SmaccsAdderImpl extends AbstractIidmExtensionAdder<Network, Smaccs> implements SmaccsAdder {

    private final List<Smacc> smaccs = new ArrayList<>();

    public SmaccsAdderImpl(Network network) {
        super(network);
    }

    @Override
    public Class<? super Smaccs> getExtensionClass() {
        return Smaccs.class;
    }

    void addSmacc(SmaccImpl smacc) {
        smaccs.add(Objects.requireNonNull(smacc));
    }

    @Override
    public SmaccAdder newSmacc() {
        return new SmaccAdderImpl(this);
    }

    @Override
    protected SmaccsImpl createExtension(Network network) {
        if (smaccs.isEmpty()) {
            throw new PowsyblException("Empty control zone list");
        }
        var smaccsVar = new SmaccsImpl(smaccs);
        for (var smacc : smaccs) {
            ((SmaccImpl) smacc).setSmaccs(smaccsVar);
        }
        return smaccsVar;
    }

    NetworkImpl getNetwork() {
        return (NetworkImpl) extendable;
    }
}
