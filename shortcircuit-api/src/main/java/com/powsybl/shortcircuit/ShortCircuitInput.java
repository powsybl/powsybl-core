/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic input for short circuit-computations.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShortCircuitInput {

    // VERSION = 1.0
    public static final String VERSION = "1.0";

    private final List<Fault> faults = new ArrayList<>();

    public List<Fault> getFaults() {
        return faults;
    }

    public ShortCircuitInput setFaults(List<Fault> faults) {
        this.faults.addAll(List.copyOf(faults));
        return this;
    }
}
