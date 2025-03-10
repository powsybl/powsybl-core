/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

/**
 * An ExtensionProvider able to serialize/deserialize extensions from XML.
 * <p>
 * An ExtensionSerializer can have several versions with one XSD schema per version: the XML serialization/deserialization of an extension is versionable.
 *
 *
 *
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface ExtensionSerDe<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E>, Versionable {

    /**
     * Return the XSD schema describing the extension to serialize in the latest version of its XML serialization.
     */
    InputStream getXsdAsStream();

    /**
     * Return the list of all XSD schemas describing the extension to serialize. <br>
     * There is a distinct XSD schema for each version of its XML serialization.
     */
    default List<InputStream> getXsdAsStreamList() {
        return Collections.singletonList(getXsdAsStream());
    }

    /**
     * Return the namespace URI of the extension in the latest version of its XML serialization.
     */
    String getNamespaceUri();

    /**
     * Return the namespace URI of the extension in a given version of its XML serialization.
     */
    default String getNamespaceUri(String extensionVersion) {
        return getNamespaceUri();
    }

    String getNamespacePrefix();

    void write(E extension, SerializerContext context);

    E read(T extendable, DeserializerContext context);

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
     * Return the version corresponding to the given namespace URI
     */
    String getVersion(String namespaceUri);

    /**
     * Return all supported versions for serializing this extension.
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

    /**
     * Provides the map whose keys are the array field names and whose values are the single element field names.
     * This is used to deduce the name of an element inside and array.
     */
    default Map<String, String> getArrayNameToSingleNameMap() {
        return Collections.emptyMap();
    }

    default boolean postponeDeserialization() {
        return false;
    }

    default Function<T, E> readAndGetPostponableCreator(DeserializerContext context) {
        throw new IllegalStateException("readAndGetPostponableCreator has to be implemented when deserialization is postponed.");
    }
}
