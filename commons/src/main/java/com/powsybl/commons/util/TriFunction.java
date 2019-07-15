/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;


/**
 * @author Chamseddine Benhamed <Chamseddine.Benhamed at rte-france.com>
 */

@FunctionalInterface
public interface TriFunction<F, S, T, R> {
    public R apply(F first, S second, T third);
}
