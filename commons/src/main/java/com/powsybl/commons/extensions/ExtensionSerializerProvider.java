/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class ExtensionSerializerProvider<T extends ExtensionSerializer> {

    private final Map<String, T> serializers;

    ExtensionSerializerProvider(Class<T> clazz) {
        serializers = new ServiceLoaderCache<>(clazz).getServices().stream()
            .collect(Collectors.toMap(T::getExtensionName, e -> e));
    }

    ExtensionSerializerProvider(Class<T> clazz, String categoryName) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(categoryName);

        serializers = new ServiceLoaderCache<>(clazz).getServices().stream()
            .filter(s -> s.getCategoryName().equals(categoryName))
            .collect(Collectors.toMap(T::getExtensionName, e -> e));
    }

    public T findSerializer(String name) {
        return serializers.get(name);
    }

    public T findSerializerOrThrowException(String name) {
        T serializer = findSerializer(name);
        if (serializer == null) {
            throw new PowsyblException("Serializer not found for extension " + name);
        }

        return serializer;
    }

    public Collection<T> getSerializers() {
        return serializers.values();
    }

    public <T> void addExtensions(Extendable<T> extendable, Collection<Extension<T>> extensions) {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(extensions);

        extensions.forEach(e -> extendable.addExtension(findSerializer(e.getName()).getExtensionClass(), e));
    }
}
