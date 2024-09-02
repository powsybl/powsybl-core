/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.impl.VoltageRegulationAdderImpl;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class VoltageRegulationAdderImplProvider implements ExtensionAdderProvider<Battery, VoltageRegulation, VoltageRegulationAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return VoltageRegulation.NAME;
    }

    @Override
    public Class<VoltageRegulationAdderImpl> getAdderClass() {
        return VoltageRegulationAdderImpl.class;
    }

    @Override
    public VoltageRegulationAdderImpl newAdder(Battery battery) {
        return new VoltageRegulationAdderImpl(battery);
    }
}
