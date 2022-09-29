/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifiant;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class UcteIdentifier implements Identifier {
    private final String voltageLevelId1;
    private final String voltageLevelId2;
    private final int order;

    public UcteIdentifier(String voltageLevelId1, String voltageLevelId2, int order) {
        this.voltageLevelId1 = voltageLevelId1;
        this.voltageLevelId2 = voltageLevelId2;
        if (order <= 0) {
            throw new IllegalArgumentException("order must be an integer > 0");
        }
        this.order = order;
    }

    @Override
    public Contingency filterIdentifiable(Network network) {
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevelId1);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevelId2);
        if (voltageLevel1 == null || voltageLevel2 == null) {
            return null;
        } else {
            List<Connectable> connectablesVoltageLevel2 = voltageLevel2.getConnectableStream().collect(Collectors.toList());
            List<Connectable> foundConnectables = voltageLevel1.getConnectableStream()
                    .filter(connectable -> connectable.getId().endsWith(String.valueOf(order)) &&
                            connectablesVoltageLevel2.contains(connectable))
                    .collect(Collectors.toList());
            if (foundConnectables.size() == 1) {
                return new Contingency(foundConnectables.get(0).getId(), Contingency.getContingencyElement(foundConnectables.get(0)));
            } else {
                return null;
            }
        }
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.UCTE;
    }

    public String getVoltageLevelId1() {
        return voltageLevelId1;
    }

    public String getVoltageLevelId2() {
        return voltageLevelId2;
    }

    public int getOrder() {
        return order;
    }
}
