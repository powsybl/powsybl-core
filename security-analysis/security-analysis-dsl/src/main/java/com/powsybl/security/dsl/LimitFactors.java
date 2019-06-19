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

import java.util.Optional;

/**
 * A definition of factors to be applied to security limits.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
interface LimitFactors {

    /**
     * Get the limit factor, if defined, for the specified {@link Branch},
     * side, current limit and contingency.
     *
     * @param contingency The contingency for which a factor is requested, or {@code null} for N situation.
     * @param branch      The branch for which a factor is requested.
     * @param side        The side for which a factor is requested.
     * @param limit       The temporary limit for which a factor is requested, or {@code null} for permanent limit.
     * @return            The factor to be applied to the specified limit, or empty if none matches.
     */
    Optional<Float> getFactor(Contingency contingency, Branch branch, Branch.Side side, CurrentLimits.TemporaryLimit limit);

}
