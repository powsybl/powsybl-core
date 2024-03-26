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
     * The temporary limit under which the current is.
     * In particular, it determines the duration during which
     * the current current value may be sustained.
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
}
