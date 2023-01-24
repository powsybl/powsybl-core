/**
 * Copyright (c) 2023
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

/**
 * Interface to apply the values read from a AMPL solve.
 * For now it allows to modify the behavior on generators only.
 * As future developments might create applyBatteries etc... we don't want to
 * use this as a functional interface.
 */
public interface NetworkApplier {

    public static NetworkApplier getDefaultApplier() {
        return new DefaultNetworkApplier();
    }

    /**
     * This class implements the default behavior that was already implemented in
     * {@link AmplNetworkReader}.
     */
    public class DefaultNetworkApplier implements NetworkApplier {

        public void applyGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP,
                double targetQ, double p, double q) {
            g.setVoltageRegulatorOn(vregul);

            g.setTargetP(targetP);
            g.setTargetQ(targetQ);

            Terminal t = g.getTerminal();
            t.setP(p).setQ(q);

            double vb = t.getVoltageLevel().getNominalV();
            g.setTargetV(targetV * vb);
        }
    }

    public void applyGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP, double targetQ,
            double p, double q);
}
