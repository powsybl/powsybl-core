package com.powsybl.cgmes.conversion.test.update;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.update.ChangesListener;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;

public final class UpdateLoadsGenerators {
    private UpdateLoadsGenerators() {
    }

    public static void updateNetwork(Network network) {
        changes = new ArrayList<>();
        ChangesListener changeListener = new ChangesListener(changes);
        network.addListener(changeListener);

        try {

            for (Generator g : network.getGenerators()) {
                double newP = g.getTargetP() * 0.1;
                double newQ = g.getTargetQ() * 0.1;
                g.setTargetP(newP).setTargetQ(newQ).getTerminal().setP(-newP).setQ(-newQ);
            }

            network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "1");
            network.getVariantManager().setWorkingVariant("1");

            for (Load g : network.getLoads()) {
                double newP = g.getP0() * 0.1;
                double newQ = g.getQ0() * 0.1;
                g.setP0(newP).setQ0(newQ).getTerminal().setP(newP).setQ(newQ);
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }

    }

    static List<IidmChange> changes;
    private static final Logger LOG = LoggerFactory.getLogger(UpdateLoadsGenerators.class);
}
