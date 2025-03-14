/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class MultiBundleMessageTemplateProvider implements MessageTemplateProvider {

    private final String[] bundleBaseNames;

    public MultiBundleMessageTemplateProvider(String... bundleBaseNames) {
        this.bundleBaseNames = bundleBaseNames;
    }

    @Override
    public String getTemplate(String key, Locale locale) {
        for (String bundleBaseName : bundleBaseNames) {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseName, locale);
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        throw new MissingResourceException("Could not find template for key '{}'", PropertyResourceBundle.class.getName(), key);
    }
}
