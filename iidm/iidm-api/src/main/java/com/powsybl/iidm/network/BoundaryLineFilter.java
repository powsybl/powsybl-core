/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.function.Predicate;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public enum BoundaryLineFilter {
    /** All boundary lines, that is, no filtering */
    ALL(bl -> true),

    /** Only paired boundary lines */
    PAIRED(BoundaryLine::isPaired),

    /** Only unpaired boundary lines */
    UNPAIRED(Predicate.not(BoundaryLine::isPaired));

    private final Predicate<BoundaryLine> predicate;

    BoundaryLineFilter(Predicate<BoundaryLine> predicate) {
        this.predicate = predicate;
    }

    public Predicate<BoundaryLine> getPredicate() {
        return this.predicate;
    }
}
