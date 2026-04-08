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
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurements;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class MeasurementsAdderImplProvider<C extends Connectable<C>> implements ExtensionAdderProvider<C, Measurements<C>, MeasurementsAdderImpl<C>> {
    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return Measurements.NAME;
    }

    @Override
    public Class<? super MeasurementsAdderImpl<C>> getAdderClass() {
        return MeasurementsAdderImpl.class;
    }

    @Override
    public MeasurementsAdderImpl<C> newAdder(C extendable) {
        return new MeasurementsAdderImpl<>(extendable);
    }
}
