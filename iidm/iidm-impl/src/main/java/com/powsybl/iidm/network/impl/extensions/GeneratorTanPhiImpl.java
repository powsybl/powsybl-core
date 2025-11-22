/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorTanPhi;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class GeneratorTanPhiImpl extends AbstractExtension<Generator> implements GeneratorTanPhi {

    private double tanPhi;
    private boolean tanPhiRegulated;

    // TODO Is there a need for a checkTanPhi (as the checkCode in GeneratorEntsoeCategoryImpl ?

    public GeneratorTanPhiImpl(Generator generator, double tanPhi, boolean tanPhiRegulated) {
        super(generator);
        this.tanPhi = tanPhi;
        this.tanPhiRegulated = tanPhiRegulated;
    }

    @Override
    public double getTanPhi() {
        return tanPhi;
    }

    @Override
    public GeneratorTanPhiImpl setTanPhi(double tanPhi) {
        this.tanPhi = tanPhi;
        return this;
    }

    @Override
    public boolean isTanPhiRegulated() {
        return tanPhiRegulated;
    }

    @Override
    public GeneratorTanPhi setTanPhiRegulated(boolean tanPhiRegulated) {
        this.tanPhiRegulated = tanPhiRegulated;
        return this;
    }
}
