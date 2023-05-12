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
public enum DanglingLineFilter {
    /** All dangling lines, that is, no filtering */
    ALL(dl -> true),

    /** Only paired dangling lines */
    PAIRED(BoundaryLine::isPaired),

    /** Only unpaired dangling lines */
    UNPAIRED(Predicate.not(BoundaryLine::isPaired));

    private final Predicate<BoundaryLine> predicate;

    DanglingLineFilter(Predicate<BoundaryLine> predicate) {
        this.predicate = predicate;
    }

    public Predicate<BoundaryLine> getPredicate() {
        return this.predicate;
    }
}
