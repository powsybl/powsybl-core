/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

/**
 * @author Yichen Tang {@literal <yichen.tang at rte-france.com>}
 */
public final class IidmSerDeConstants {

    public static final String INDENT = "    ";

    public static final IidmVersion CURRENT_IIDM_VERSION = IidmVersion.V_1_12;

    public static final String IIDM_PREFIX = "iidm";

    public static final String ITESLA_DOMAIN = "itesla_project.eu";
    public static final String POWSYBL_DOMAIN = "powsybl.org";

    private IidmSerDeConstants() {
    }
}
