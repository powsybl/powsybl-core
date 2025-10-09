/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Branch;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface BranchObservabilityAdder<B extends Branch<B>>
        extends ExtensionAdder<B, BranchObservability<B>> {

    @Override
    default Class<BranchObservability> getExtensionClass() {
        return BranchObservability.class;
    }

    BranchObservabilityAdder<B> withObservable(boolean observable);

    BranchObservabilityAdder<B> withStandardDeviationP1(double standardDeviationP1);

    BranchObservabilityAdder<B> withStandardDeviationP2(double standardDeviationP2);

    BranchObservabilityAdder<B> withRedundantP1(Boolean redundantP1);

    BranchObservabilityAdder<B> withRedundantP2(Boolean redundantP2);

    BranchObservabilityAdder<B> withStandardDeviationQ1(double standardDeviationQ1);

    BranchObservabilityAdder<B> withStandardDeviationQ2(double standardDeviationQ2);

    BranchObservabilityAdder<B> withRedundantQ1(Boolean redundantQ1);

    BranchObservabilityAdder<B> withRedundantQ2(Boolean redundantQ2);
}
