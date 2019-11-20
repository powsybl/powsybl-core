/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_VERSION_UNDERSCORE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
final class IidmXmlTestConstants {

    static final String IIDM_CURRENT_VERSION_DIR_NAME = "/V" + IIDM_VERSION_UNDERSCORE + "/";

    // directory names of previous IIDM versions
    static final String IIDM_VERSION_1_0_DIR_NAME = "/V1_0/";

    private IidmXmlTestConstants() {
    }
}
