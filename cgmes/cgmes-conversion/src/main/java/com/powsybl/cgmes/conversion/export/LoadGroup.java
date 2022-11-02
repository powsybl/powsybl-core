/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class LoadGroup {
    final String className;
    final String id;
    final String name;

    LoadGroup(String className, String id, String name) {
        this.className = className;
        this.id = id;
        this.name = name;
    }
}
