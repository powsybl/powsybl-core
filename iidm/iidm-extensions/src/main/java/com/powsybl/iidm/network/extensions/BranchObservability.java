/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Branch;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public interface BranchObservability<B extends Branch<B>> extends Extension<B>, Observability<B> {

    @Override
    default String getName() {
        return "branchObservability";
    }

    /** StandardDeviation for Active Power */
    double getStandardDeviationP(Branch.Side side);

    BranchObservability<B> setStandardDeviationP(double standardDeviationP, Branch.Side side);

    boolean isRedundantP(Branch.Side side);

    BranchObservability<B> setRedundantP(boolean redundant, Branch.Side side);

    /** StandardDeviation for Reactive Power */
    double getStandardDeviationQ(Branch.Side side);

    BranchObservability<B> setStandardDeviationQ(double standardDeviationQ, Branch.Side side);

    boolean isRedundantQ(Branch.Side side);

    BranchObservability<B> setRedundantQ(boolean redundant, Branch.Side side);

    /** StandardDeviation for Voltage amplitude */
    double getStandardDeviationV(Branch.Side side);

    BranchObservability<B> setStandardDeviationV(double standardDeviationV, Branch.Side side);

    boolean isRedundantV(Branch.Side side);

    BranchObservability<B> setRedundantV(boolean redundant, Branch.Side side);
}
