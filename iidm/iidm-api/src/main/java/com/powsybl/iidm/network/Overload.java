/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
  * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
  */
public interface Overload {
    /**
     * The temporary limit determining the duration of time at which the present current
     * may be sustained.This depends on the {@link DetectionKind}:
     * <ul>
     *     <li>{@link DetectionKind#HIGH}: the temporary limit whose value is directly above the present current</li>
     *     <li>{@link DetectionKind#LOW}: the temporary limit whose value is directly under the present current (this corresponds to {@link #getPreviousLimit()}</li>
     * </ul>
     */
    LoadingLimits.TemporaryLimit getTemporaryLimit();

    /**
     * The value of the current limit which has been overloaded, in Amperes.
     */
    double getPreviousLimit();

    /**
     * The name of the current limit which has been overloaded.
     */
    String getPreviousLimitName();

    /**
      * @return the id of the {@link OperationalLimitsGroup} this overload relates to
      */
    String getOperationalLimitsGroupId();

    default double getLimitReductionCoefficient() {
        return 1;
    }
}
