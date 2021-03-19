/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public enum Marker {
    PERFORMANCE(0);

    private final int level;

    Marker(int level) {
        this.level = level;
    }
}
