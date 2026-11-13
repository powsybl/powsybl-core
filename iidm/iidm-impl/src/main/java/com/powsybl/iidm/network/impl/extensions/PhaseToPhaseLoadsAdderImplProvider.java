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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoads;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoadsAdder;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class PhaseToPhaseLoadsAdderImplProvider implements ExtensionAdderProvider<Load, PhaseToPhaseLoads, PhaseToPhaseLoadsAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return PhaseToPhaseLoads.NAME;
    }

    @Override
    public Class<PhaseToPhaseLoadsAdder> getAdderClass() {
        return PhaseToPhaseLoadsAdder.class;
    }

    @Override
    public PhaseToPhaseLoadsAdder newAdder(Load load) {
        return new PhaseToPhaseLoadsAdderImpl(load);
    }
}
