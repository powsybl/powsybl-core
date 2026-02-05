/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineMutualCoupling;
import com.powsybl.iidm.network.extensions.LineMutualCouplingAdder;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class LineMutualCouplingAdderImpl extends AbstractExtensionAdder<Line, LineMutualCoupling> implements LineMutualCouplingAdder {

    private double r = Double.NaN;
    private double x = Double.NaN;

    public LineMutualCouplingAdderImpl(Line line) {
        super(line);
    }

    @Override
    public Class<? super LineMutualCoupling> getExtensionClass() {
        return LineMutualCoupling.class;
    }

    @Override
    protected LineMutualCouplingImpl createExtension(Line line) {
        return new LineMutualCouplingImpl(line, r, x);
    }

    public LineMutualCouplingAdderImpl withR(double r) {
        this.r = r;
        return this;
    }

    public LineMutualCouplingAdderImpl withX(double x) {
        this.x = x;
        return this;
    }
}
