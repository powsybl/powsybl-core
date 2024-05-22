/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public interface Area extends Identifiable<Area> {

    AreaType getAreaType();

    Iterable<VoltageLevel> getVoltageLevels();

    Stream<VoltageLevel> getVoltageLevelStream();

    void addVoltageLevel(VoltageLevel voltageLevel);

    Map<BoundaryPointType, List<Terminal>> getBoundaryPointsByType();

    Iterable<Terminal> getBoundaryPoints();

    Stream<Terminal> getBoundaryPointStream();

    Iterable<Terminal> getBoundaryPoints(BoundaryPointType boundaryPointType);

    Stream<Terminal> getBoundaryPointStream(BoundaryPointType boundaryPointType);

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA;
    }

    /**
     * Get the optional target AC Net Interchange of this area in MW, using load sign convention
     * @return the AC Net Interchange target
     */
    Optional<Double> getAcNetInterchangeTarget();

    /**
     * Get the optional net interchange tolerance in MW
     * @return the net interchange tolerance
     */
    Optional<Double> getAcNetInterchangeTolerance();
}
