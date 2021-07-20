/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Branch;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public interface BranchObservabilityAdder<B extends Branch<B>>
        extends ExtensionAdder<B, BranchObservability<B>> {

    @Override
    default Class<BranchObservability> getExtensionClass() {
        return BranchObservability.class;
    }

    BranchObservabilityAdder<B> withObservable(boolean observable);

    BranchObservabilityAdder<B> withStandardDeviationP(double standardDeviationP, Branch.Side side);

    BranchObservabilityAdder<B> withRedundantP(boolean redundant, Branch.Side side);

    BranchObservabilityAdder<B> withStandardDeviationQ(double standardDeviationQ, Branch.Side side);

    BranchObservabilityAdder<B> withRedundantQ(boolean redundant, Branch.Side side);

    BranchObservabilityAdder<B> withStandardDeviationV(double standardDeviationV, Branch.Side side);

    BranchObservabilityAdder<B> withRedundantV(boolean redundant, Branch.Side side);
}
