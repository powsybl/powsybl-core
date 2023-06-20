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
public class DanglingLineFilter {
    /** All dangling lines, that is, no filtering */
    public static final DanglingLineFilter ALL = new DanglingLineFilter(dl -> true);

    /** Only paired dangling lines */
    public static final DanglingLineFilter PAIRED = new DanglingLineFilter(DanglingLine::isPaired);

    /** Only unpaired dangling lines */
    public static final DanglingLineFilter UNPAIRED = new DanglingLineFilter(PAIRED.getPredicate().negate());

    private final Predicate<DanglingLine> predicate;

    DanglingLineFilter(Predicate<DanglingLine> predicate) {
        this.predicate = predicate;
    }

    public Predicate<DanglingLine> getPredicate() {
        return this.predicate;
    }

    private static DanglingLineFilter pairedOf(Network network) {
        return network == null ? PAIRED : new DanglingLineFilter(dl -> dl.isPaired(network));
    }

    private static DanglingLineFilter unpairedOf(Network network) {
        return network == null ? UNPAIRED : new DanglingLineFilter(dl -> !dl.isPaired(network));
    }

    public DanglingLineFilter restrictTo(Network network) {
        if (network != null) {
            if (this == PAIRED) {
                return pairedOf(network);
            } else if (this == UNPAIRED) {
                return unpairedOf(network);
            }
        }
        return this;
    }
}
