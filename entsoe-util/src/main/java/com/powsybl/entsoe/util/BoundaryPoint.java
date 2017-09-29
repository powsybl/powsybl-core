/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.Country;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BoundaryPoint {

    private final String name;

    private final Country borderFrom;

    private final Country borderTo;

    public BoundaryPoint(String name, Country borderFrom, Country borderTo) {
        this.name = Objects.requireNonNull(name);
        this.borderFrom = Objects.requireNonNull(borderFrom);
        this.borderTo = Objects.requireNonNull(borderTo);
    }

    public String getName() {
        return name;
    }

    public Country getBorderFrom() {
        return borderFrom;
    }

    public Country getBorderTo() {
        return borderTo;
    }

    @Override
    public String toString() {
        return name;
    }
}
