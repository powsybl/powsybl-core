/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class BatteryShortCircuitAdderImpl extends AbstractExtensionAdder<Battery, BatteryShortCircuit>
    implements BatteryShortCircuitAdder {

    double directTransX = 0;
    double directSubtransX = Double.NaN;
    double stepUpTransformerX = Double.NaN;

    protected BatteryShortCircuitAdderImpl(Battery extendable) {
        super(extendable);
    }

    @Override
    protected BatteryShortCircuit createExtension(Battery extendable) {
        return new BatteryShortCircuitImpl(extendable, directSubtransX, directTransX, stepUpTransformerX);
    }

    @Override
    public BatteryShortCircuitAdder withDirectTransX(double directTransX) {
        this.directTransX = directTransX;
        return this;
    }

    @Override
    public BatteryShortCircuitAdder withDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return this;
    }

    @Override
    public BatteryShortCircuitAdder withStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return this;
    }

    @Override
    public BatteryShortCircuit add() {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        return super.add();
    }
}
