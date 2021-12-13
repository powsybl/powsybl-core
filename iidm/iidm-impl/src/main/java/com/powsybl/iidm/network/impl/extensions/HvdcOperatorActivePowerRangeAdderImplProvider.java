/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class HvdcOperatorActivePowerRangeAdderImplProvider
        implements ExtensionAdderProvider<HvdcLine, HvdcOperatorActivePowerRange, HvdcOperatorActivePowerRangeAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionsName() {
        return HvdcOperatorActivePowerRange.NAME;
    }

    @Override
    public Class<HvdcOperatorActivePowerRangeAdderImpl> getAdderClass() {
        return HvdcOperatorActivePowerRangeAdderImpl.class;
    }

    @Override
    public HvdcOperatorActivePowerRangeAdderImpl newAdder(HvdcLine extendable) {
        return new HvdcOperatorActivePowerRangeAdderImpl(extendable);
    }
}
