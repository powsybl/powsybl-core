/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency;

/**
 * This type precises the contingencies involved.
 * If only pre-contingency state matters, NONE must be used.
 * If all the post-contingency states plus the pre-contingency state matters, ALL must be used.
 * If all the post-contingency states without the pre-contingency state matters, ONLY_CONTINGENCIES must be used.
 * A SPECIFIC contingency context focus on a single contingency, which id is specified in the {@link ContingencyContext}.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */

public enum ContingencyContextType {
    /**
     *  Corresponds to all contingencies and the pre-contingency state.
     */
    ALL,
    /**
     *  Corresponds to the pre-contingency state only.
     */
    NONE,
    /**
     *  Corresponds to the contingency whose id is specified in the contingency context.
     */
    SPECIFIC,
    /**
     *  Corresponds to all contingencies, without the pre-contingency state.
     */
    ONLY_CONTINGENCIES
}
