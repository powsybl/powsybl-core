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
import com.powsybl.iidm.network.extensions.OperatingStatus;

/**
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class OperatingStatusAdderImplProvider<I extends Identifiable<I>> implements
        ExtensionAdderProvider<I, OperatingStatus<I>, OperatingStatusAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<OperatingStatusAdderImpl> getAdderClass() {
        return OperatingStatusAdderImpl.class;
    }

    @Override
    public OperatingStatusAdderImpl<I> newAdder(I identifiable) {
        return new OperatingStatusAdderImpl<>(identifiable);
    }
}
