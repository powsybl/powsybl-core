/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

/**
 * describes when an action is taken
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public interface Condition {
    String getType();
}
