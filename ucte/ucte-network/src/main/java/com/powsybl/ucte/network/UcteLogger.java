/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
public class UcteLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteLogger.class);

    public void warn(String ruleId, String message, String cause, Object... parameters) {
        LOGGER.warn("{} - {} ({})", ruleId, message, String.format(cause, parameters));
    }

    public void error(String ruleId, String message, String cause, Object... parameters) {
        LOGGER.error("{} - {} ({})", ruleId, message, String.format(cause, parameters));
    }
}
