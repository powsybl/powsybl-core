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

    /** StandardDeviation for Active Power */
    double getStandardDeviationP();

    InjectionObservability<I> setStandardDeviationP(double standardDeviationP);

    boolean isRedundantP();

    InjectionObservability<I> setRedundantP(boolean redundant);

    /** StandardDeviation for Reactive Power */
    double getStandardDeviationQ();

    InjectionObservability<I> setStandardDeviationQ(double standardDeviationQ);

    boolean isRedundantQ();

    InjectionObservability<I> setRedundantQ(boolean redundant);

    /** StandardDeviation for Voltage amplitude */
    double getStandardDeviationV();

    InjectionObservability<I> setStandardDeviationV(double standardDeviationV);

    boolean isRedundantV();

    InjectionObservability<I> setRedundantV(boolean redundant);
}
