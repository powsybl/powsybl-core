/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Branch;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface BranchObservability<B extends Branch<B>> extends Extension<B>, Observability<B> {

    String NAME = "branchObservability";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Optional standard deviation for active power at side ONE of the branch (in MW).
     * @return nullable
     */
    ObservabilityQuality<B> getQualityP1();

    BranchObservability<B> setQualityP1(double standardDeviation, Boolean redundant);

    BranchObservability<B> setQualityP1(double standardDeviation);

    /**
     * Optional standard deviation for active power at side TWO of the branch (in MW).
     * @return nullable
     */
    ObservabilityQuality<B> getQualityP2();

    BranchObservability<B> setQualityP2(double standardDeviation, Boolean redundant);

    BranchObservability<B> setQualityP2(double standardDeviation);

    /**
     * StandardDeviation for reactive power at side ONE of the branch (in MVar).
     * @return nullable
     */
    ObservabilityQuality<B> getQualityQ1();

    BranchObservability<B> setQualityQ1(double standardDeviation, Boolean redundant);

    BranchObservability<B> setQualityQ1(double standardDeviation);

    /**
     * StandardDeviation for reactive power at side TWO of the branch (in MVar).
     * @return nullable
     */
    ObservabilityQuality<B> getQualityQ2();

    BranchObservability<B> setQualityQ2(double standardDeviation, Boolean redundant);

    BranchObservability<B> setQualityQ2(double standardDeviation);
}
