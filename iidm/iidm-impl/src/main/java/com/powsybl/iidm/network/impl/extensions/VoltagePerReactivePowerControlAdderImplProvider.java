/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class VoltagePerReactivePowerControlAdderImplProvider implements
        ExtensionAdderProvider<StaticVarCompensator, VoltagePerReactivePowerControl, VoltagePerReactivePowerControlAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return VoltagePerReactivePowerControl.NAME;
    }

    @Override
    public Class<VoltagePerReactivePowerControlAdderImpl> getAdderClass() {
        return VoltagePerReactivePowerControlAdderImpl.class;
    }

    @Override
    public VoltagePerReactivePowerControlAdderImpl newAdder(StaticVarCompensator svc) {
        return new VoltagePerReactivePowerControlAdderImpl(svc);
    }
}
