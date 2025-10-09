/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class FortescueConstants {

    private FortescueConstants() {
    }

    public static final boolean DEFAULT_GROUNDED = false;
    public static final double DEFAULT_GROUNDING_R = 0;
    public static final double DEFAULT_GROUNDING_X = 0;
    public static final boolean DEFAULT_FREE_FLUXES = true;
    public static final WindingConnectionType DEFAULT_LEG1_CONNECTION_TYPE = WindingConnectionType.DELTA;
    public static final WindingConnectionType DEFAULT_LEG2_CONNECTION_TYPE = WindingConnectionType.Y_GROUNDED;
    public static final WindingConnectionType DEFAULT_LEG3_CONNECTION_TYPE = WindingConnectionType.DELTA;
}
