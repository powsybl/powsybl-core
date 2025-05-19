/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * This class implements the default behavior of applying changes to the network. <br>
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 * @see AmplNetworkReader
 * @see AmplNetworkUpdater
 */
public class DefaultAmplNetworkUpdater extends AbstractAmplNetworkUpdater {

    private final StringToIntMapper<AmplSubset> networkMapper;

    public DefaultAmplNetworkUpdater(StringToIntMapper<AmplSubset> networkMapper) {
        Objects.requireNonNull(networkMapper);
        this.networkMapper = networkMapper;
    }

    public void updateNetworkGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP,
                                        double targetQ, double p, double q) {
        g.setVoltageRegulatorOn(vregul);

        g.setTargetP(targetP);
        g.setTargetQ(targetQ);

        Terminal t = g.getTerminal();
        t.setP(p).setQ(q);

        double nominalV = g.getRegulatingTerminal().getVoltageLevel().getNominalV();
        g.setTargetV(targetV * nominalV);
        busConnection(t, busNum, networkMapper);
    }

    public void updateNetworkVsc(VscConverterStation vsc, int busNum, boolean vregul, double targetV, double targetQ,
                                 double p, double q) {
        Terminal t = vsc.getTerminal();
        t.setP(p).setQ(q);

        vsc.setReactivePowerSetpoint(targetQ);
        vsc.setVoltageRegulatorOn(vregul);

        double nominalV = vsc.getRegulatingTerminal().getVoltageLevel().getNominalV();
        vsc.setVoltageSetpoint(targetV * nominalV);
        busConnection(t, busNum, networkMapper);
    }

    public void updateNetworkBattery(Battery b, int busNum, double targetP, double targetQ, double p, double q) {
        b.setTargetP(targetP);
        b.setTargetQ(targetQ);

        Terminal t = b.getTerminal();
        t.setP(p).setQ(q);
        busConnection(t, busNum, networkMapper);
    }

    public void updateNetworkSvc(StaticVarCompensator svc, int busNum, boolean vregul, double targetV, double q) {
        if (vregul) {
            svc.setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
        } else {
            if (q == 0) {
                svc.setRegulating(false);
            } else {
                svc.setReactivePowerSetpoint(-q);
            }
            svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
        }

        Terminal t = svc.getTerminal();
        t.setQ(q);
        double nominalV = svc.getRegulatingTerminal().getVoltageLevel().getNominalV();
        svc.setVoltageSetpoint(targetV * nominalV);
        busConnection(t, busNum, networkMapper);
    }

    public void updateNetworkShunt(ShuntCompensator sc, int busNum, double q, double b, int sections) {
        if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            // TODO improve non linear shunt section count update.
        } else {
            sc.setSectionCount(Math.max(0, Math.min(sc.getMaximumSectionCount(), sections)));
        }
        sc.getTerminal().setQ(q);
    }

    public void updateNetworkLoad(Load l, Network network, String id, int busNum, double p, double q, double p0,
                                  double q0) {
        if (l != null) {
            l.setP0(p0).setQ0(q0);
            l.getTerminal().setP(p).setQ(q);
            busConnection(l.getTerminal(), busNum, networkMapper);
        } else {
            DanglingLine dl = network.getDanglingLine(id);
            if (dl != null) {
                dl.setP0(p0).setQ0(q0);
                dl.getTerminal().setP(p).setQ(q);
                busConnection(dl.getTerminal(), busNum, networkMapper);
            } else {
                throw new AmplException("Invalid load id '" + id + "'");
            }
        }
    }

    @Override
    public void updateNetworkRatioTapChanger(Network network, String id, int tap) {
        if (id.endsWith(AmplConstants.LEG1_SUFFIX) || id.endsWith(AmplConstants.LEG2_SUFFIX) || id.endsWith(
                AmplConstants.LEG3_SUFFIX)) {
            ThreeWindingsTransformer twt = getThreeWindingsTransformer(network, id);
            RatioTapChanger rtc = getThreeWindingsTransformerLeg(twt, id).getRatioTapChanger();
            rtc.setTapPosition(rtc.getLowTapPosition() + tap - 1);
        } else {
            TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
            if (twt == null) {
                throw new AmplException("Invalid two windings transformer id '" + id + "'");
            }
            RatioTapChanger rtc = twt.getRatioTapChanger();
            rtc.setTapPosition(rtc.getLowTapPosition() + tap - 1);
        }
    }

    public void updateNetworkPhaseTapChanger(Network network, String id, int tap) {
        if (id.endsWith(AmplConstants.LEG1_SUFFIX) || id.endsWith(AmplConstants.LEG2_SUFFIX) || id.endsWith(
                AmplConstants.LEG3_SUFFIX)) {
            ThreeWindingsTransformer twt = getThreeWindingsTransformer(network, id);
            PhaseTapChanger ptc = getThreeWindingsTransformerLeg(twt, id).getPhaseTapChanger();
            ptc.setTapPosition(ptc.getLowTapPosition() + tap - 1);
        } else {
            TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
            if (twt == null) {
                throw new AmplException("Invalid two windings transformer id '" + id + "'");
            }
            PhaseTapChanger ptc = twt.getPhaseTapChanger();
            ptc.setTapPosition(ptc.getLowTapPosition() + tap - 1);
        }
    }

    @Override
    public void updateNetworkBus(Bus bus, double v, double theta) {
        bus.setAngle(Math.toDegrees(theta));
        bus.setV(v * bus.getVoltageLevel().getNominalV());
    }

    @Override
    public void updateNetworkBranch(Branch br, Network network, String id, int busNum, int busNum2, double p1,
                                    double p2, double q1, double q2) {
        if (br != null) {
            br.getTerminal1().setP(p1).setQ(q1);
            br.getTerminal2().setP(p2).setQ(q2);
            busConnection(br.getTerminal1(), busNum, networkMapper);
            busConnection(br.getTerminal2(), busNum2, networkMapper);
        } else if (!readThreeWindingsTransformerBranch(network, id, p1, q1, busNum, networkMapper)) {
            DanglingLine dl = network.getDanglingLine(id);
            if (dl != null) {
                dl.getTerminal().setP(p1).setQ(q1);
                busConnection(dl.getTerminal(), busNum, networkMapper);
            } else {
                throw new AmplException("Invalid branch id '" + id + "'");
            }
        }
    }

    @Override
    public void updateNetworkHvdcLine(HvdcLine hl, String converterMode, double targetP) {
        hl.setConvertersMode(HvdcLine.ConvertersMode.valueOf(converterMode));
        hl.setActivePowerSetpoint(targetP);
    }

    @Override
    public void updateNetworkLcc(LccConverterStation lcc, int busNum, double p, double q) {
        lcc.getTerminal().setP(p).setQ(q);
        busConnection(lcc.getTerminal(), busNum, networkMapper);
    }

    private boolean readThreeWindingsTransformerBranch(Network network, String id, double p, double q, int busNum,
                                                       StringToIntMapper<AmplSubset> mapper) {
        if (id.endsWith(AmplConstants.LEG1_SUFFIX) || id.endsWith(AmplConstants.LEG2_SUFFIX) || id.endsWith(
                AmplConstants.LEG3_SUFFIX)) {
            ThreeWindingsTransformer twt = getThreeWindingsTransformer(network, id);
            Terminal terminal = getThreeWindingsTransformerLeg(twt, id).getTerminal();
            terminal.setP(p).setQ(q);
            busConnection(terminal, busNum, mapper);
            return true;
        }
        return false;
    }
}
