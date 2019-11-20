/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlTestConstants.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TieLineXmlTest extends AbstractXmlConverterTest {

    @Test
    public void test() throws IOException {
        roundTripVersionnedXmlTest("tieline.xml", IIDM_VERSION_1_0_DIR_NAME, IIDM_CURRENT_VERSION_DIR_NAME);
    }
}
