/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.extensions.BoundaryFlowLimits;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class BoundaryFlowLimitsAdderImplProvider implements ExtensionAdderProvider<DanglingLine, BoundaryFlowLimits, BoundaryFlowLimitsAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<BoundaryFlowLimitsAdderImpl> getAdderClass() {
        return BoundaryFlowLimitsAdderImpl.class;
    }

    @Override
    public BoundaryFlowLimitsAdderImpl newAdder(DanglingLine extendable) {
        return new BoundaryFlowLimitsAdderImpl(extendable);
    }
}
