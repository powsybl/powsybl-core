/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.com>}
 */
public class IdentifiableShortCircuitImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements IdentifiableShortCircuit<I> {
    private double ipMin; // Minimum allowable peak short-circuit current
    private double ipMax; // Maximum allowable peak short-circuit current

    public IdentifiableShortCircuitImpl(I extendable, double ipMin, double ipMax) {
        super(extendable);
        this.ipMin = ipMin;
        this.ipMax = ipMax;
    }

    @Override
    public double getIpMin() {
        return ipMin;
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMin(double ipMin) {
        this.ipMin = ipMin;
        return this;
    }

    @Override
    public double getIpMax() {
        return ipMax;
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMax(double ipMax) {
        if (Double.isNaN(ipMax)) {
            throw new PowsyblException("Undefined ipMax");
        }
        this.ipMax = ipMax;
        return this;
    }

}
