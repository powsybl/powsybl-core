/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface InjectionObservability<I extends Injection<I>> extends Extension<I>, Observability<I> {

    String NAME = "injectionObservability";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Optional standard deviation for active power in MW.
     * @return nullable
     */
    ObservabilityQuality<I> getQualityP();

    InjectionObservability<I> setQualityP(double standardDeviation, Boolean redundant);

    InjectionObservability<I> setQualityP(double standardDeviation);

    /**
     * StandardDeviation for reactive power in MVar.
     * @return nullable
     */
    ObservabilityQuality<I> getQualityQ();

    InjectionObservability<I> setQualityQ(double standardDeviation, Boolean redundant);

    InjectionObservability<I> setQualityQ(double standardDeviation);

    /**
     * StandardDeviation for voltage amplitude en kV.
     * @return nullable
     */
    ObservabilityQuality<I> getQualityV();

    InjectionObservability<I> setQualityV(double standardDeviation, Boolean redundant);

    InjectionObservability<I> setQualityV(double standardDeviation);
}
