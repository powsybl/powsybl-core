/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.vlequivalent;

import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public abstract class AbstractInjectionVlEquivalent {
    private final String id;
    private final String name;
    protected final double activePower;
    protected final double reactivePower;

    protected AbstractInjectionVlEquivalent(String id, String name, double activePower, double reactivePower, TwoWindingsTransformer transformer) {
        this.id = id;
        this.name = name;
        if (!transformer.hasPhaseTapChanger()) {
            this.activePower = activePower;
            this.reactivePower = reactivePower;
        } else {
            double phaseChange = transformer.getPhaseTapChanger().getCurrentStep().getAlpha();
            //apply vector rotation (rotation matrix of phaseChange)
            double cosChange = Math.cos(phaseChange);
            double sinChange = Math.sin(phaseChange);
            this.activePower = activePower * cosChange - reactivePower * sinChange;
            this.reactivePower = activePower * sinChange + reactivePower * cosChange;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
