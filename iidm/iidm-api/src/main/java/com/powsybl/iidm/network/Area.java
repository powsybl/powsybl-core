/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.stream.Stream;

public interface Area extends Identifiable<Area> {

    AreaType getAreaType();

    Iterable<VoltageLevel> getVoltageLevels();

    Stream<VoltageLevel> getVoltageLevelStream();

    void addVoltageLevel(VoltageLevel voltageLevel);

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA;
    }
}
