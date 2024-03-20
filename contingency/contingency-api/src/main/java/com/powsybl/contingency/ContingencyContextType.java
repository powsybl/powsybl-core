/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 *
 * Define for a contingencyContext the type of information asked.
 * It can be a pre-contingency state, a post-contingency state (on a specific contingency (SPECIFIC) or on every contingency (ONLY_CONTINGENCIES)) or both (ALL)
 */

public enum ContingencyContextType {
    /**
     *  Corresponds to all contingencies and pre-contingency situation
     */
    ALL,
    /**
     *  Corresponds to pre-contingency situation
     */
    NONE,
    /**
     *  Corresponds to one contingency whose id is specified in the contingencyContext
     */
    SPECIFIC,
    /**
     *  Corresponds to all contingencies (without the pre-contingency situation)
     */
    ONLY_CONTINGENCIES
}
