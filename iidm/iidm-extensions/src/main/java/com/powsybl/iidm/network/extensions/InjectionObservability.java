/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public interface InjectionObservability<I extends Injection<I>> extends Extension<I>, Observability<I> {

    @Override
    default String getName() {
        return "injectionObservability";
    }

    /**
     * Optional standard deviation for Active Power
     * @return nullable
     */
    ObservabilityQuality<I> getQualityP();

    InjectionObservability<I> setQualityP(double standardDeviation, Boolean redundant);

    /**
     * StandardDeviation for Reactive Power
     * @return nullable
     */
    ObservabilityQuality<I> getQualityQ();

    InjectionObservability<I> setQualityQ(double standardDeviation, Boolean redundant);

    /**
     * StandardDeviation for Voltage amplitude
     * @return nullable
     */
    ObservabilityQuality<I> getQualityV();

    InjectionObservability<I> setQualityV(double standardDeviation, Boolean redundant);
}
