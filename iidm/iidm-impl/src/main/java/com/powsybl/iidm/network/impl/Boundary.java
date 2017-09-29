/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.Country;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class Boundary {

    private final Country country1;
    private final Country country2;

    Boundary(Country country1, Country country2) {
        this.country1 = Objects.requireNonNull(country1);
        this.country2 = Objects.requireNonNull(country2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country1, country2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Boundary) {
            Boundary other = (Boundary) obj;
            return Sets.newHashSet(country1, country2).equals(Sets.newHashSet(other.country1, other.country2));
        }
        return false;
    }

    @Override
    public String toString() {
        return country1 + "/" + country2;
    }
}
