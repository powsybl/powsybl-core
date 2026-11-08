/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

/**
 * Loads {@link ResourceBundle}s without falling back on the default locale.
 * {@link ResourceBundle.Control} is not supported in named modules, so in that case the bundle is looked up in the
 * module declaring it and the fallback on the default locale is emulated.
 */
final class ReportBundles {

    private ReportBundles() {
    }

    static ResourceBundle getBundle(String baseName, Locale locale) {
        Module module = findModule(baseName);
        if (!module.isNamed()) {
            return ResourceBundle.getBundle(baseName, locale, MessageTemplateProvider.NO_FALLBACK_CONTROL);
        }
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, module);
        Locale bundleLocale = bundle.getLocale();
        if (!bundleLocale.equals(Locale.ROOT) && !locale.toString().startsWith(bundleLocale.toString())) {
            return ResourceBundle.getBundle(baseName, Locale.ROOT, module);
        }
        return bundle;
    }

    private static Module findModule(String baseName) {
        return ServiceLoader.load(ReportResourceBundle.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(r -> baseName.equals(r.getBaseName()))
                .map(r -> r.getClass().getModule())
                .findFirst()
                .orElse(ReportBundles.class.getModule());
    }
}
