/**
 * Copyright (c) 2023
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VscConverterStation;

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

        public void applyVsc(VscConverterStation vsc, boolean vregul, double targetV, double targetQ, double p,
                double q) {
            Terminal t = vsc.getTerminal();
            t.setP(p).setQ(q);

            vsc.setReactivePowerSetpoint(targetQ);
            vsc.setVoltageRegulatorOn(vregul);

            double vb = t.getVoltageLevel().getNominalV();
            vsc.setVoltageSetpoint(targetV * vb);
        }

        public void applyBattery(Battery b, double targetP, double targetQ, double p, double q) {
            b.setTargetP(targetP);
            b.setTargetQ(targetQ);

            Terminal t = b.getTerminal();
            t.setP(p).setQ(q);
        }

        public void applySvc(StaticVarCompensator svc, boolean vregul, double targetV, double q) {
            if (vregul) {
                svc.setRegulationMode(RegulationMode.VOLTAGE);
            } else {
                if (q == 0) {
                    svc.setRegulationMode(RegulationMode.OFF);
                } else {
                    svc.setReactivePowerSetpoint(-q);
                    svc.setRegulationMode(RegulationMode.REACTIVE_POWER);
                }
            }

            Terminal t = svc.getTerminal();
            t.setQ(q);
            double nominalV = t.getVoltageLevel().getNominalV();
            svc.setVoltageSetpoint(targetV * nominalV);
        }

        public void applyShunt(ShuntCompensator sc, double q, int sections) {
            if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
                // TODO improve non linear shunt section count update.
            } else {
                sc.setSectionCount(Math.max(0, Math.min(sc.getMaximumSectionCount(), sections)));
            }
            sc.getTerminal().setQ(q);
        }
    }

    public void applyGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP, double targetQ,
            double p, double q);

    public void applyBattery(Battery b, double targetP, double targetQ, double p, double q);

    public void applyShunt(ShuntCompensator sc, double q, int sections);

    public void applySvc(StaticVarCompensator svc, boolean vregul, double targetV, double q);

    public void applyVsc(VscConverterStation vsc, boolean vregul, double targetV, double targetQ, double p, double q);

}
