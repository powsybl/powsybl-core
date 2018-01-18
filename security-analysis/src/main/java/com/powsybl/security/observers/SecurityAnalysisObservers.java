/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.observers;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class SecurityAnalysisObservers {

    private static final Supplier<Map<String, SecurityAnalysisObserverExtension>> EXTENSIONS
        = Suppliers.memoize(SecurityAnalysisObservers::loadExtensions);

    private static Map<String, SecurityAnalysisObserverExtension> loadExtensions() {
        return new ServiceLoaderCache<>(SecurityAnalysisObserverExtension.class).getServices().stream()
            .collect(Collectors.toMap(SecurityAnalysisObserverExtension::getName, e -> e));
    }

    public static Set<String> getExtensionNames() {
        return EXTENSIONS.get().keySet();
    }

    public static SecurityAnalysisObserver createObserver(String name) {
        Objects.requireNonNull(name);

        SecurityAnalysisObserverExtension extension = EXTENSIONS.get().get(name);

        return extension == null ? null : extension.createObserver();
    }

    private SecurityAnalysisObservers() {
    }
}
