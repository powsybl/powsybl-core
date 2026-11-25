/**
 * Copyright (c) 2026, Artelys (https://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.util.ServiceLoaderCache;

/**
 * Holds the JVM-wide cache of {@link ReportResourceBundle} base names.
 * Lazily computed on first access, class initialization guaranteeing thread safety.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
final class ClasspathResourceBundlesCache {

    private static final String[] BUNDLE_BASE_NAMES = new ServiceLoaderCache<>(ReportResourceBundle.class).getServices().stream()
            .map(ReportResourceBundle::getBaseName)
            .toArray(String[]::new);

    private ClasspathResourceBundlesCache() {
    }

    static String[] getBaseNames() {
        // Defensive copy: MultiBundleMessageTemplateProvider keeps the given array without copying it
        return BUNDLE_BASE_NAMES.clone();
    }
}
