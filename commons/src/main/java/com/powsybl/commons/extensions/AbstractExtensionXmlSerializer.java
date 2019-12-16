/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.PowsyblException;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractExtensionXmlSerializer<T extends Extendable, E extends Extension<T>> implements ExtensionXmlSerializer<T, E> {

    private final String extensionName;

    private final String categoryName;

    private final Class<? super E> extensionClass;

    private final boolean subElements;

    private final String xsdFileName;

    private final String namespaceUri;

    private final String namespacePrefix;

    protected AbstractExtensionXmlSerializer(String extensionName, String categoryName, Class<? super E> extensionClass,
                                             boolean subElements, String xsdFileName, String namespaceUri, String namespacePrefix) {
        this.extensionName = Objects.requireNonNull(extensionName);
        this.categoryName = Objects.requireNonNull(categoryName);
        this.extensionClass = Objects.requireNonNull(extensionClass);
        this.subElements = subElements;
        this.xsdFileName = Objects.requireNonNull(xsdFileName);
        this.namespaceUri = Objects.requireNonNull(namespaceUri);
        this.namespacePrefix = Objects.requireNonNull(namespacePrefix);
    }

    protected AbstractExtensionXmlSerializer(String extensionName, String categoryName, Class<? super E> extensionClass,
                                             boolean subElements, String namespacePrefix) {
        this.extensionName = Objects.requireNonNull(extensionName);
        this.categoryName = Objects.requireNonNull(categoryName);
        this.extensionClass = Objects.requireNonNull(extensionClass);
        this.subElements = subElements;
        this.xsdFileName = null;
        this.namespaceUri = null;
        this.namespacePrefix = Objects.requireNonNull(namespacePrefix);
    }

    @Override
    public String getExtensionName() {
        return extensionName;
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public Class<? super E> getExtensionClass() {
        return extensionClass;
    }

    public boolean hasSubElements() {
        return subElements;
    }

    @Override
    public InputStream getXsdAsStream() {
        if (xsdFileName != null) {
            return getClass().getResourceAsStream("/xsd/" + xsdFileName);
        }
        throw new PowsyblException("Undefined xsd file name");
    }

    @Override
    public String getNamespaceUri() {
        if (namespaceUri != null) {
            return namespaceUri;
        }
        throw new PowsyblException("Undefined namespace URI");
    }

    @Override
    public String getNamespacePrefix() {
        return namespacePrefix;
    }
}
