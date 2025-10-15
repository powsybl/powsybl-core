package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.TapChangerBlocking;
import com.powsybl.iidm.network.extensions.TapChangerBlockingAdder;
import com.powsybl.iidm.network.extensions.TapChangerBlockings;
import com.powsybl.iidm.network.extensions.TapChangerBlockingsAdder;
import com.powsybl.iidm.network.impl.NetworkImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class TapChangerBlockingsAdderImpl extends AbstractIidmExtensionAdder<Network, TapChangerBlockings> implements TapChangerBlockingsAdder {

    private final List<TapChangerBlocking> tcbs = new ArrayList<>();

    public TapChangerBlockingsAdderImpl(Network network) {
        super(network);
    }

    @Override
    public Class<? super TapChangerBlockings> getExtensionClass() {
        return TapChangerBlockings.class;
    }

    void addTapChangerBlocking(TapChangerBlockingImpl tcb) {
        tcbs.add(Objects.requireNonNull(tcb));
    }

    @Override
    public TapChangerBlockingAdder newTapChangerBlocking() {
        return new TapChangerBlockingAdderImpl(this);
    }

    @Override
    protected TapChangerBlockingsImpl createExtension(Network network) {
        if (tcbs.isEmpty()) {
            throw new PowsyblException("Empty control zone list");
        }
        var tcbsVar = new TapChangerBlockingsImpl(tcbs);
        for (var tcb : tcbs) {
            ((TapChangerBlockingImpl) tcb).setTapChangerBlockings(tcbsVar);
        }
        return tcbsVar;
    }

    NetworkImpl getNetwork() {
        return (NetworkImpl) extendable;
    }
}
