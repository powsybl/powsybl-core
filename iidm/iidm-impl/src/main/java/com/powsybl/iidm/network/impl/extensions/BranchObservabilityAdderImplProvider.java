/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class BranchObservabilityAdderImplProvider<B extends Branch<B>> implements
        ExtensionAdderProvider<B, BranchObservability<B>, BranchObservabilityAdderImpl<B>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return BranchObservability.NAME;
    }

    @Override
    public Class<BranchObservabilityAdderImpl> getAdderClass() {
        return BranchObservabilityAdderImpl.class;
    }

    @Override
    public BranchObservabilityAdderImpl<B> newAdder(B extendable) {
        return new BranchObservabilityAdderImpl<>(extendable);
    }
}
