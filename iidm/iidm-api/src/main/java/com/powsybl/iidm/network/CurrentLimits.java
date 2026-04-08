/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * Current limits are defined by:
 * <ul>
 *     <li>A permanent limit (A)</li>
 *     <li>
 * Any number of temporary limits.
 * A temporary limit has an acceptable duration (s).
 * The branch can safely stay between the previous limit (could be another temporary limit or the permanent limit) and
 * this limit during the acceptable duration.
 * A NaN temporay limit value means infinite.
 *     </li>
 * </ul>
 *
 *<p>
 * The following diagram shows current areas and corresponding acceptable duration for a permanent limit and 3 temporary
 * limits with x, y and z acceptable durations.
 * <div>
 *    <object data="doc-files/currentLimits.svg" type="image/svg+xml"></object>
 * </div>
 *</p>
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface CurrentLimits extends LoadingLimits {

    @Override
    default LimitType getLimitType() {
        return LimitType.CURRENT;
    }
}
