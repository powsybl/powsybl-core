/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import java.util.Deque;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public interface Printable {
    void pushTo(Deque<Object> stack);
}
