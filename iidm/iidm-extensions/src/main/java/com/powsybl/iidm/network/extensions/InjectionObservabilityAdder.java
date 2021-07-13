/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Injection;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public interface InjectionObservabilityAdder <I extends Injection<I>>
        extends ExtensionAdder<I, InjectionObservability<I>> {

    @Override
    default Class<InjectionObservability> getExtensionClass() {
        return InjectionObservability.class;
    }

    InjectionObservabilityAdder<I> withObservable(boolean observable);

    InjectionObservabilityAdder<I> withStandardDeviationP(float standardDeviationP);

    InjectionObservabilityAdder<I> withRedundantP(boolean redundant);

    InjectionObservabilityAdder<I> withStandardDeviationQ(float standardDeviationQ);

    InjectionObservabilityAdder<I> withRedundantQ(boolean redundant);

    InjectionObservabilityAdder<I> withStandardDeviationV(float standardDeviationV);

    InjectionObservabilityAdder<I> withRedundantV(boolean redundant);
}
