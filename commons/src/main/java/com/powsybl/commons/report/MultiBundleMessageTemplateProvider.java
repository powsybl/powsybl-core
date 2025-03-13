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
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class MultiBundleMessageTemplateProvider implements MessageTemplateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiBundleMessageTemplateProvider.class);

    private final String[] bundleBaseNames;

    public MultiBundleMessageTemplateProvider(String... bundleBaseNames) {
        this.bundleBaseNames = bundleBaseNames;
    }

    @Override
    public String getTemplate(String key, Locale locale) {
        for (String bundleBaseName : bundleBaseNames) {
            try {
                return ResourceBundle.getBundle(bundleBaseName, locale).getString(key);
            } catch (MissingResourceException e) {
                LOGGER.warn("Could not find template for key '{}'", key, e);
            }
        }
        throw new MissingResourceException("Could not find template for key '{}'", PropertyResourceBundle.class.getName(), key);
    }
}
