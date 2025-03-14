/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Bundle message template provider based on a {@link ResourceBundle}.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BundleMessageTemplateProvider implements MessageTemplateProvider {

    private final String bundleBaseName;
    private final boolean throwIfUnknownKey;
    private ResourceBundle resourceBundle;

    public BundleMessageTemplateProvider(String bundleBaseName) {
        this(bundleBaseName, false);
    }

    public BundleMessageTemplateProvider(String bundleBaseName, boolean throwIfUnknownKey) {
        this.bundleBaseName = bundleBaseName;
        this.resourceBundle = ResourceBundle.getBundle(bundleBaseName, ReportConstants.DEFAULT_LOCALE);
        this.throwIfUnknownKey = throwIfUnknownKey;
    }

    @Override
    public String getTemplate(String key, Locale locale) {
        if (!this.resourceBundle.getLocale().equals(locale)) {
            this.resourceBundle = ResourceBundle.getBundle(bundleBaseName, locale);
        }
        if (!throwIfUnknownKey) {
            if (!resourceBundle.containsKey(key)) {
                return MessageTemplateProvider.getMissingKeyMessage(key, locale);
            }
        }
        return resourceBundle.getString(key);
    }
}
