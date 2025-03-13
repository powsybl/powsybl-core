/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorTanPhi;
import com.powsybl.iidm.network.extensions.GeneratorTanPhiAdder;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class GeneratorTanPhiAdderImpl extends AbstractExtensionAdder<Generator, GeneratorTanPhi>
        implements GeneratorTanPhiAdder {

    private double tanPhi;
    private boolean tanPhiRegulated;

    public GeneratorTanPhiAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected GeneratorTanPhi createExtension(Generator extendable) {
        return new GeneratorTanPhiImpl(extendable, tanPhi, tanPhiRegulated);
    }

    @Override
    public GeneratorTanPhiAdder withTanPhi(double tanPhi) {
        this.tanPhi = tanPhi;
        return this;
    }

    @Override
    public GeneratorTanPhiAdder withTanPhiRegulated(boolean tanPhiRegulated) {
        this.tanPhiRegulated = tanPhiRegulated;
        return this;
    }
}
