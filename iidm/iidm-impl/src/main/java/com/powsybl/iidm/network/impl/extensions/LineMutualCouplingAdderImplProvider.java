/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineMutualCoupling;
import com.powsybl.iidm.network.extensions.LineMutualCouplingAdder;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class LineMutualCouplingAdderImplProvider implements ExtensionAdderProvider<Line, LineMutualCoupling, LineMutualCouplingAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LineMutualCoupling.NAME;
    }

    @Override
    public Class<LineMutualCouplingAdder> getAdderClass() {
        return LineMutualCouplingAdder.class;
    }

    @Override
    public LineMutualCouplingAdder newAdder(Line extendable) {
        return new LineMutualCouplingAdderImpl(extendable);
    }

}
