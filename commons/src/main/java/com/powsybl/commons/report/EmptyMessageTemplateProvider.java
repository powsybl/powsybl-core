/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Empty template provider used when deserializing a report node
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class EmptyMessageTemplateProvider implements MessageTemplateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmptyMessageTemplateProvider.class);

    @Override
    public String getTemplate(String key, Locale locale) {
        LOGGER.warn("Returning empty template for key {}, please change the MessageTemplateProvider used", key);
        return "";
    }
}
