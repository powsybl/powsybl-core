/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
abstract class AbstractFeederResult implements FeederResult {

    private final String connectableId;

    protected AbstractFeederResult(String connectableId) {
        this.connectableId = Objects.requireNonNull(connectableId);
    }

    public String getConnectableId() {
        return connectableId;
    }

}
