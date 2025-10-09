/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Injection;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface InjectionObservabilityAdder <I extends Injection<I>>
        extends ExtensionAdder<I, InjectionObservability<I>> {

    @Override
    default Class<InjectionObservability> getExtensionClass() {
        return InjectionObservability.class;
    }

    InjectionObservabilityAdder<I> withObservable(boolean observable);

    InjectionObservabilityAdder<I> withStandardDeviationP(double standardDeviationP);

    InjectionObservabilityAdder<I> withRedundantP(Boolean redundant);

    InjectionObservabilityAdder<I> withStandardDeviationQ(double standardDeviationQ);

    InjectionObservabilityAdder<I> withRedundantQ(Boolean redundant);

    InjectionObservabilityAdder<I> withStandardDeviationV(double standardDeviationV);

    InjectionObservabilityAdder<I> withRedundantV(Boolean redundant);
}
