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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class DiscreteMeasurementsAdderImplProvider<I extends Identifiable<I>> implements ExtensionAdderProvider<I, DiscreteMeasurements<I>, DiscreteMeasurementsAdderImpl<I>> {
    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return DiscreteMeasurements.NAME;
    }

    @Override
    public Class<? super DiscreteMeasurementsAdderImpl<I>> getAdderClass() {
        return DiscreteMeasurementsAdderImpl.class;
    }

    @Override
    public DiscreteMeasurementsAdderImpl<I> newAdder(I extendable) {
        return new DiscreteMeasurementsAdderImpl<>(extendable);
    }
}
