/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.jupiter.api.Assertions.fail;

import com.powsybl.commons.PowsyblException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
final class TestUtil {

    private TestUtil() {
    }

    static void notImplemented(final Runnable execute) {
        // Using Try/catch in order to make sure executed code is covered by test
        try {
            execute.run();
            fail("Implementation done -> this test must be updated");
        } catch (final PowsyblException ex) {
            // Ignored
        }
    }
}
