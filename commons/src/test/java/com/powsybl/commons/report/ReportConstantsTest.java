/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.test.AbstractSerDeTest;
import org.junit.jupiter.api.Test;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
class ReportConstantsTest extends AbstractSerDeTest {

    @Test
    void testGetDefaultReportLocale() {
        Locale.setDefault(Locale.US);
        assertEquals(Locale.US, ReportConstants.getDefaultLocale());

        Locale.setDefault(Locale.FRENCH);
        assertEquals(Locale.FRENCH, ReportConstants.getDefaultLocale());
    }
}
