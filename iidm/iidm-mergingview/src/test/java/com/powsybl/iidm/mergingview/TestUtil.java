/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.fail;

import com.powsybl.commons.PowsyblException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class TestUtil {

    private static final String NOT_IMPLEMENTED = "Implementation done -> this test must be updated";

    private TestUtil() {
    }

    static void notImplemented(final Runnable execute) {
        try {
            execute.run();
            fail(NOT_IMPLEMENTED);
        } catch (final PowsyblException ex) {
            // Ignored
        }
    }
}
