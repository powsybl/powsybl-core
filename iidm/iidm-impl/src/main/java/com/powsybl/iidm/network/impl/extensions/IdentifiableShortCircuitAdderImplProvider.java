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
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;


/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class IdentifiableShortCircuitAdderImplProvider<I extends Identifiable<I>>
    implements ExtensionAdderProvider<I, IdentifiableShortCircuit<I>, IdentifiableShortCircuitAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return IdentifiableShortCircuit.NAME;
    }

    @Override
    public Class<? super IdentifiableShortCircuitAdderImpl> getAdderClass() {
        return IdentifiableShortCircuitAdderImpl.class;
    }

    @Override
    public IdentifiableShortCircuitAdderImpl<I> newAdder(I extendable) {
        return new IdentifiableShortCircuitAdderImpl<>(extendable);
    }
}
