/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class ExtensionSupplier {

    private static final Supplier<Map<String, ExtensionJson>> EXTENSIONS_JSON_SUPPLIER
        = Suppliers.memoize(ExtensionSupplier::loadExtensionJson);

    private static Map<String, ExtensionJson> loadExtensionJson() {
        return new ServiceLoaderCache<>(ExtensionJson.class).getServices().stream()
            .collect(Collectors.toMap(ExtensionJson::getExtensionName, e -> e));
    }

    public static ExtensionJson findExtensionJson(String name) {
        return EXTENSIONS_JSON_SUPPLIER.get().get(name);
    }

    public static ExtensionJson findExtensionJsonOrThrowException(String name) {
        ExtensionJson extensionJson = findExtensionJson(name);
        if (extensionJson == null) {
            throw new PowsyblException("Json serializer not found for extension " + name);
        }

        return extensionJson;
    }

    public static <T> void addExtensions(Extendable<T> extendable, Collection<Extension<T>> extensions) {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(extensions);

        extensions.forEach(e -> extendable.addExtension(findExtensionJson(e.getName()).getExtensionClass(), e));
    }

    private ExtensionSupplier() {
    }
}
