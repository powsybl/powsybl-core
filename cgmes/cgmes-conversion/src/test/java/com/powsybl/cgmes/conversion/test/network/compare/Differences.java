/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.network.compare;

import com.powsybl.iidm.network.Identifiable;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface Differences {

    // Every time we use context we must be aware of possible current
    void current(Identifiable i);

    void compare(String context, double expected, double actual, double tolerance);

    void compare(String context, Object expected, Object actual);

    void unexpected(Identifiable actual);

    void missing(Identifiable expected);

    // Unexpected value for given property in current Identifiable
    void unexpected(String property);

    // Missing property in current Identifiable
    void missing(String property);

    void match(Identifiable actual);

    void notEquivalent(String context, Identifiable expected, Identifiable actual);

    void end();
}
