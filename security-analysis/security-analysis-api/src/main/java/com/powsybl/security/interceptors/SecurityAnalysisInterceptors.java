/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class SecurityAnalysisInterceptors {

    private static final Supplier<Map<String, SecurityAnalysisInterceptorExtension>> EXTENSIONS
        = Suppliers.memoize(SecurityAnalysisInterceptors::loadExtensions);

    private static Map<String, SecurityAnalysisInterceptorExtension> loadExtensions() {
        return new ServiceLoaderCache<>(SecurityAnalysisInterceptorExtension.class).getServices().stream()
            .collect(Collectors.toMap(SecurityAnalysisInterceptorExtension::getName, e -> e));
    }

    public static Set<String> getExtensionNames() {
        return EXTENSIONS.get().keySet();
    }

    public static SecurityAnalysisInterceptor createInterceptor(String name) {
        Objects.requireNonNull(name);

        SecurityAnalysisInterceptorExtension extension = EXTENSIONS.get().get(name);
        if (extension == null) {
            throw new IllegalArgumentException("The extension '" + name + "' doesn't exist");
        }

        return extension.createInterceptor();
    }

    private SecurityAnalysisInterceptors() {
    }
}
