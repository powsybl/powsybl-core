/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;

/**
 *
 * A predicate for current limits selection. The current limit and the context will be described by:
 *  - the {@link Branch} of the limit
 *  - the {@link Branch.Side} of the limit
 *  - the {@link CurrentLimits.TemporaryLimit} in case it is a temporary limit
 *  - the {@link Contingency} in case we are in a post-contingency context
 */
interface LimitMatcher {

    /**
     * Determine if a current limit and the context match the underlying criteria
     *
     * @param branch        the branch of the limit
     * @param side          the side of the limit
     * @param limit         the temporary limit in case it is a temporary one,
     *                      or {@code null} in case it is a permanent limit
     * @param contingency   the contingency if we are in a post-contingency context,
     *                      {@code null} if we are in N situation
     *
     * @return              true if the limit and the context match the underlying criteria
     */
    boolean matches(Branch branch, Branch.Side side, CurrentLimits.TemporaryLimit limit, Contingency contingency);
}
