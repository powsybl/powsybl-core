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
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class InjectionObservabilityAdderImplProvider<I extends Injection<I>> implements
        ExtensionAdderProvider<I, InjectionObservability<I>, InjectionObservabilityAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return InjectionObservability.NAME;
    }

    @Override
    public Class<InjectionObservabilityAdderImpl> getAdderClass() {
        return InjectionObservabilityAdderImpl.class;
    }

    @Override
    public InjectionObservabilityAdderImpl<I> newAdder(I extendable) {
        return new InjectionObservabilityAdderImpl<>(extendable);
    }
}
