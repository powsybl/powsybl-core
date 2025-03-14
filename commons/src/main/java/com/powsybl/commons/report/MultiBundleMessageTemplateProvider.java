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
    private final boolean throwIfUnknownKey;

    public MultiBundleMessageTemplateProvider(String... bundleBaseNames) {
        this(false, bundleBaseNames);
    }

    public MultiBundleMessageTemplateProvider(boolean throwIfUnknownKey, String... bundleBaseNames) {
        this.bundleBaseNames = bundleBaseNames;
        this.throwIfUnknownKey = throwIfUnknownKey;
    }

    @Override
    public String getTemplate(String key, Locale locale) {
        for (String bundleBaseName : bundleBaseNames) {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseName, locale);
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        if (throwIfUnknownKey) {
            throw new MissingResourceException("Could not find template for key '{}'", PropertyResourceBundle.class.getName(), key);
        } else {
            return "Unknown key: '" + key + "'";
        }
    }
}
