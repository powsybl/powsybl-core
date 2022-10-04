/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifier;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class VoltageLevelAndOrderNetworkElementIdentifier implements NetworkElementIdentifier {

    private static final Logger LOG = LoggerFactory.getLogger(VoltageLevelAndOrderNetworkElementIdentifier.class);
    private final String voltageLevelId1;
    private final String voltageLevelId2;
    private final char order;

    public VoltageLevelAndOrderNetworkElementIdentifier(String voltageLevelId1, String voltageLevelId2, char order) {
        this.voltageLevelId1 = Objects.requireNonNull(voltageLevelId1);
        this.voltageLevelId2 = Objects.requireNonNull(voltageLevelId2);
        this.order = Objects.requireNonNull(order);
    }

    @Override
    public Optional<Identifiable> filterIdentifiable(Network network) {
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevelId1);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevelId2);
        if (voltageLevel1 == null || voltageLevel2 == null) {
            return Optional.empty();
        } else {
            List<Connectable> connectablesVoltageLevel2 = voltageLevel2.getConnectableStream().collect(Collectors.toList());
            List<Connectable> foundConnectables = voltageLevel1.getConnectableStream()
                    .filter(connectable -> connectable.getId().endsWith(String.valueOf(order)) &&
                            connectablesVoltageLevel2.contains(connectable))
                    .collect(Collectors.toList());
            if (foundConnectables.size() == 1) {
                return Optional.ofNullable(foundConnectables.get(0));
            } else {
                LOG.warn("found several connectables between voltage levels " + voltageLevel1.getId() +
                        " and " + voltageLevel2.getId() + " with order " + order);
                return Optional.empty();
            }
        }
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.VOLTAGE_LEVELS_AND_ORDER;
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
