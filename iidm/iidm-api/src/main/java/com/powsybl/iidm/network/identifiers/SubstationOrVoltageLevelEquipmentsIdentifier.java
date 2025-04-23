/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.identifiers;

import com.powsybl.iidm.network.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class SubstationOrVoltageLevelEquipmentsIdentifier implements NetworkElementIdentifier {

    private final String substationOrVoltageLevelId;
    private final Set<IdentifiableType> voltageLevelIdentifiableTypes;

    public SubstationOrVoltageLevelEquipmentsIdentifier(String substationOrVoltageLevelId) {
        this(substationOrVoltageLevelId, Set.of());
    }

    public SubstationOrVoltageLevelEquipmentsIdentifier(String substationOrVoltageLevelId, Set<IdentifiableType> voltageLevelIdentifiableTypes) {
        this.substationOrVoltageLevelId = substationOrVoltageLevelId;
        this.voltageLevelIdentifiableTypes = voltageLevelIdentifiableTypes;
    }

    public String getSubstationOrVoltageLevelId() {
        return substationOrVoltageLevelId;
    }

    public Set<IdentifiableType> getVoltageLevelIdentifiableTypes() {
        return voltageLevelIdentifiableTypes;
    }

    @Override
    public Set<Identifiable> filterIdentifiable(Network network) {
        VoltageLevel voltageLevel = network.getVoltageLevel(substationOrVoltageLevelId);
        Substation substation = network.getSubstation(substationOrVoltageLevelId);
        Stream<Connectable> connectableStream;
        if (voltageLevel != null) {
            connectableStream = voltageLevel.getConnectableStream();
        } else if (substation != null) {
            connectableStream = substation.getVoltageLevelStream().flatMap(VoltageLevel::getConnectableStream);
        } else {
            return Set.of();
        }
        if (voltageLevelIdentifiableTypes.isEmpty()) {
            return connectableStream.collect(Collectors.toSet());
        }
        return connectableStream.filter(connectable -> voltageLevelIdentifiableTypes.contains(connectable.getType()))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getNotFoundElements(Network network) {
        VoltageLevel voltageLevel = network.getVoltageLevel(substationOrVoltageLevelId);
        Substation substation = network.getSubstation(substationOrVoltageLevelId);
        if (voltageLevel == null && substation == null) {
            return Set.of(substationOrVoltageLevelId);
        }
        return Set.of();
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.SUBSTATION_OR_VOLTAGE_LEVEL_EQUIPMENTS;
    }

    @Override
    public Optional<String> getContingencyId() {
        return Optional.ofNullable(substationOrVoltageLevelId);
    }

    @Override
    public Boolean isMonoElementContingencies() {
        return true;
    }
}
