/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.modification;

import java.util.OptionalDouble;

/**
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
public abstract class AbstractLoadModification extends AbstractNetworkModification {
    protected final Double p0;
    protected final Double q0;
    protected final boolean relativeValue;

    AbstractLoadModification(Double p0, Double q0, boolean relativeValue) {
        this.p0 = p0;
        this.q0 = q0;
        this.relativeValue = relativeValue;
    }

    public boolean isRelativeValue() {
        return relativeValue;
    }

    public OptionalDouble getP0() {
        return p0 == null ? OptionalDouble.empty() : OptionalDouble.of(p0);
    }

    public OptionalDouble getQ0() {
        return q0 == null ? OptionalDouble.empty() : OptionalDouble.of(q0);
    }
}
