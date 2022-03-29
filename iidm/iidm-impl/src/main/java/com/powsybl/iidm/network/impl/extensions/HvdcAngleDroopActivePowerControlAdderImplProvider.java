/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class HvdcAngleDroopActivePowerControlAdderImplProvider
        implements ExtensionAdderProvider<HvdcLine, HvdcAngleDroopActivePowerControl, HvdcAngleDroopActivePowerControlAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return HvdcAngleDroopActivePowerControl.NAME;
    }

    @Override
    public Class<? super HvdcAngleDroopActivePowerControlAdder> getAdderClass() {
        return HvdcAngleDroopActivePowerControlAdder.class;
    }

    @Override
    public HvdcAngleDroopActivePowerControlAdderImpl newAdder(HvdcLine extendable) {
        return new HvdcAngleDroopActivePowerControlAdderImpl(extendable);
    }
}
