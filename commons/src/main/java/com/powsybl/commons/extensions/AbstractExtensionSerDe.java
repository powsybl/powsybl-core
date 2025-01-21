/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import java.io.InputStream;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractExtensionSerDe<T extends Extendable, E extends Extension<T>> implements ExtensionSerDe<T, E> {

    private final String extensionName;

    private final String categoryName;

    private final Class<? super E> extensionClass;

    private final String xsdFileName;

    private final String namespaceUri;

    private final String namespacePrefix;

    protected AbstractExtensionSerDe(String extensionName, String categoryName, Class<? super E> extensionClass,
                                     String xsdFileName, String namespaceUri, String namespacePrefix) {
        this.extensionName = Objects.requireNonNull(extensionName);
        this.categoryName = Objects.requireNonNull(categoryName);
        this.extensionClass = Objects.requireNonNull(extensionClass);
        this.xsdFileName = Objects.requireNonNull(xsdFileName);
        this.namespaceUri = Objects.requireNonNull(namespaceUri);
        this.namespacePrefix = Objects.requireNonNull(namespacePrefix);
    }

    @Override
    public String getExtensionName() {
        return extensionName;
    }

    @Override
    public final String getSerializationName(String extensionVersion) {
        return getExtensionName();
    }

    @Override
    public final Set<String> getSerializationNames() {
        return Set.of(getExtensionName());
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public Class<? super E> getExtensionClass() {
        return extensionClass;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/" + xsdFileName);
    }

    @Override
    public String getNamespaceUri() {
        return namespaceUri;
    }

    @Override
    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    @Override
    public String getVersion(String namespaceUri) {
        return this.namespaceUri.equals(namespaceUri) ? getVersion() : null;
    }
}
