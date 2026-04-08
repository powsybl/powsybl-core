/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.NetworkModification;

/**
 * An action is taken according to an operator strategy when a condition occurs.
 * It aims at solving violations.
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public interface Action {

    String getType();

    String getId();

    default NetworkModification toModification() {
        throw new UnsupportedOperationException("toModification not implemented");
    }
}
