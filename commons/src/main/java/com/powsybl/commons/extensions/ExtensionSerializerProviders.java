/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class ExtensionSerializerProviders {

    public static <T extends ExtensionSerializer> ExtensionSerializerProvider<T> createProvider(Class<T> clazz) {
        return new ExtensionSerializerProvider<>(clazz);
    }

    public static <T extends ExtensionSerializer> ExtensionSerializerProvider<T> createProvider(Class<T> clazz, String categoryName) {
        return new ExtensionSerializerProvider<>(clazz, categoryName);
    }

    private ExtensionSerializerProviders() {
    }
}
