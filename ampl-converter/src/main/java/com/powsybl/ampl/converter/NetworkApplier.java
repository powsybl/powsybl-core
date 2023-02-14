/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;

/**
 * Interface to modify a network after an Ampl solve.<br>
 * {@link AmplNetworkReader} does the Ampl output parsing,
 * and {@link NetworkApplier} modifies the {@link Network}.<br>
 * Also provides some utility functions for implementations :
 * <ul>
 *     <li>{@link NetworkApplier#busConnection}</li>
 *     <li>{@link NetworkApplier#getThreeWindingsTransformerLeg}</li>
 *     <li>{@link NetworkApplier#getThreeWindingsTransformer}</li>
 * </ul>
 * Default implementation in {@link DefaultNetworkApplier}.
 *
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 * @see AmplNetworkReader
 * @see DefaultNetworkApplier
 */
public interface NetworkApplier {

    static AbstractNetworkApplierFactory getDefaultApplierFactory() {
        return new AbstractNetworkApplierFactory() {
            @Override
            public NetworkApplier of(StringToIntMapper<AmplSubset> mapper, Network network) {
                return new DefaultNetworkApplier(mapper, network);
            }
        };
    }

    void applyGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP, double targetQ,
                         double p, double q);

    void applyBattery(Battery b, int busNum, double targetP, double targetQ, double p, double q);

    void applyShunt(ShuntCompensator sc, int busNum, double q, double b, int sections);

    void applySvc(StaticVarCompensator svc, int busNum, boolean vregul, double targetV, double q);

    void applyVsc(VscConverterStation vsc, int busNum, boolean vregul, double targetV, double targetQ, double p,
                  double q);

    void applyLoad(Load l, Network network, String id, int busNum, double p, double q, double p0, double q0);

    void applyRatioTapChanger(Network network, String id, int tap);

    void applyPhaseTapChanger(Network network, String id, int tap);

    void applyBus(Bus bus, double v, double theta);

    void applyBranch(Branch br, Network network, String id, int busNum, int busNum2, double p1, double p2, double q1,
                     double q2);

    void applyHvdcLine(HvdcLine hl, String converterMode, double targetP);

    void applyLcc(LccConverterStation lcc, int busNum, double p, double q);

    void applyReactiveSlack(int busNum, double slackCondensator, double slackSelf, String id, String substationId);

    static void busConnection(Terminal t, int busNum, StringToIntMapper<AmplSubset> mapper) {
        if (busNum == -1) {
            t.disconnect();
        } else {
            String busId = mapper.getId(AmplSubset.BUS, busNum);
            Bus connectable = AmplUtil.getConnectableBus(t);
            if (connectable != null && connectable.getId().equals(busId)) {
                t.connect();
            }
        }
    }

    static ThreeWindingsTransformer.Leg getThreeWindingsTransformerLeg(ThreeWindingsTransformer twt, String legId) {
        if (legId.endsWith(AmplConstants.LEG1_SUFFIX)) {
            return twt.getLeg1();
        } else if (legId.endsWith(AmplConstants.LEG2_SUFFIX)) {
            return twt.getLeg2();
        } else if (legId.endsWith(AmplConstants.LEG3_SUFFIX)) {
            return twt.getLeg3();
        }

        throw new IllegalArgumentException("Unexpected suffix: " + legId.substring(legId.length() - 5));
    }

    /**
     * Return a 3 windings transformer from one its leg ID
     *
     * @param legId   The ID of a 3WT leg
     * @param network The IIDM network to update
     * @return A three windings transformer or null if not found
     */
    static ThreeWindingsTransformer getThreeWindingsTransformer(Network network, String legId) {
        String twtId = legId.substring(0, legId.length() - 5);
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer(twtId);
        if (twt == null) {
            throw new AmplException("Unable to find transformer '" + twtId + "'");
        }
        return twt;
    }

}
