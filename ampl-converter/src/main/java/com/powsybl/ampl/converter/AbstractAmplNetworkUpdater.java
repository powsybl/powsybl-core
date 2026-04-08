/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * Provides some utility functions for implementations :
 * <ul>
 *     <li>{@link AbstractAmplNetworkUpdater#busConnection}</li>
 *     <li>{@link AbstractAmplNetworkUpdater#getThreeWindingsTransformerLeg}</li>
 *     <li>{@link AbstractAmplNetworkUpdater#getThreeWindingsTransformer}</li>
 * </ul>
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public abstract class AbstractAmplNetworkUpdater implements AmplNetworkUpdater {

    public void busConnection(Terminal t, int busNum, StringToIntMapper<AmplSubset> mapper) {
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

    public ThreeWindingsTransformer.Leg getThreeWindingsTransformerLeg(ThreeWindingsTransformer twt, String legId) {
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
    public ThreeWindingsTransformer getThreeWindingsTransformer(Network network, String legId) {
        String twtId = legId.substring(0, legId.length() - 5);
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer(twtId);
        if (twt == null) {
            throw new AmplException("Unable to find transformer '" + twtId + "'");
        }
        return twt;
    }

}
