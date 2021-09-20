/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An ExtensionProvider able to serialize/deserialize extensions from XML.
 * <p>
 * An ExtensionXmlSerializer can have several versions with one XSD schema per version: the XML serialization/deserialization of an extension is versionable.
 *
 *
 *
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface ExtensionXmlSerializer<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E>, Versionable {

    boolean hasSubElements();

    /**
     * Return the XSD schema describing the extension to serialize in the latest version of its serialization.
     */
    InputStream getXsdAsStream();

    /**
     * Return the list of all XSD schemas describing the extension to serialize. <br>
     * There is a distinct XSD schema for each version of its serialization.
     */
    default List<InputStream> getXsdAsStreamList() {
        return Collections.singletonList(getXsdAsStream());
    }

    /**
     * Return the namespace URI of the extension in the latest version of its serialization.
     */
    String getNamespaceUri();

    /**
     * Return the namespace URI of the extension in a given version of its serialization.
     */
    default String getNamespaceUri(String extensionVersion) {
        return getNamespaceUri();
    }

    String getNamespacePrefix();

    void write(E extension, XmlWriterContext context) throws XMLStreamException;

    E read(T extendable, XmlReaderContext context) throws XMLStreamException;

    default String getName() {
        return getExtensionName();
    }

    /**
     * Return the latest version of the serialization of the extension.
     */
    default String getVersion() {
        return "1.0";
    }

    /**
     * Return all supported versions for of the serialization of this extension.
     */
    default Set<String> getVersions() {
        return Collections.singleton("1.0");
    }

    /**
     * Check that a given extension version exists.
     */
    default void checkExtensionVersionSupported(String extensionVersion) {
        if (!"1.0".equals(extensionVersion)) {
            throw new PowsyblException("The version " + extensionVersion + " of the " + getExtensionName() + " extension's XML serializer is not supported.");
        }
    }

    /**
     * Check if an extension can be serialized or not.
     * @param extension the extension to check
     * @return true if the extension can be serialized, false otherwise
     */
    default boolean isSerializable(E extension) {
        return true;
    }
}
