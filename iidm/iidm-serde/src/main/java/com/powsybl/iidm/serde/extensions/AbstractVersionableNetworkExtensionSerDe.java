/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractVersionableNetworkExtensionSerDe<T extends Extendable, E extends Extension<T>, V extends SerDeVersion<V>>
        implements ExtensionSerDe<T, E> {

    private static final String INCOMPATIBILITY_NETWORK_VERSION_MESSAGE = "IIDM version of network (";

    private final String extensionName;
    private final Class<? super E> extensionClass;
    private final List<V> versions;

    protected AbstractVersionableNetworkExtensionSerDe(String extensionName, Class<? super E> extensionClass, V[] versions) {
        this.extensionName = Objects.requireNonNull(extensionName);
        this.extensionClass = Objects.requireNonNull(extensionClass);
        this.versions = Arrays.stream(Objects.requireNonNull(versions))
                .sorted(Comparator.comparing(SerDeVersion::getVersionNumbers))
                .toList();
    }

    @Override
    public String getExtensionName() {
        return extensionName;
    }

    @Override
    public String getSerializationName(String extensionVersion) {
        return versionOf(extensionVersion).getSerializationName();
    }

    @Override
    public Set<String> getSerializationNames() {
        return versions.stream().map(SerDeVersion::getSerializationName).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super E> getExtensionClass() {
        return extensionClass;
    }

    @Override
    public String getNamespaceUri() {
        return getNamespaceUri(getVersion());
    }

    @Override
    public Stream<String> getNamespaceUriStream() {
        return versions.stream().map(SerDeVersion::getNamespaceUri);
    }

    @Override
    public String getNamespaceUri(String extensionVersion) {
        return versionOf(extensionVersion, true).getNamespaceUri();
    }

    @Override
    public String getVersion() {
        return getVersion(IidmSerDeConstants.CURRENT_IIDM_VERSION).getVersionString();
    }

    public boolean versionExists(IidmVersion networkVersion) {
        return versions.stream().anyMatch(version -> version.supports(networkVersion));
    }

    /**
     * Get the greatest version of an extension working with a network version.
     */
    public V getVersion(IidmVersion networkVersion) {
        // We should return the default version even if it is not the greatest in SemVer terms
        V defaultVersion = getDefaultVersion();
        if (defaultVersion.supports(networkVersion)) {
            return defaultVersion;
        }
        for (int i = versions.size() - 1; i >= 0; i--) {
            V v = versions.get(i);
            if (v.supports(networkVersion)) {
                return v;
            }
        }
        throw new PowsyblException("IIDM version " + networkVersion + " is not supported by " + extensionName + " extension");
    }

    @Override
    public String getVersion(String namespaceUri) {
        return versions.stream()
                .filter(version -> version.getNamespaceUri().equals(namespaceUri))
                .map(SerDeVersion::getVersionString)
                .findFirst()
                .orElseThrow(() -> new PowsyblException("The namespace URI " + namespaceUri + " of the " + extensionName + " extension is not supported."));
    }

    @Override
    public Set<String> getVersions() {
        return versions.stream().map(SerDeVersion::getVersionString).collect(Collectors.toUnmodifiableSet());
    }

    protected V getExtensionVersionImported(DeserializerContext context) {
        return ((NetworkDeserializerContext) context).getExtensionVersion(this)
                .map(this::versionOf)
                .orElseThrow(IllegalStateException::new);
    }

    protected V getExtensionVersionToExport(SerializerContext context) {
        NetworkSerializerContext networkSerializerContext = (NetworkSerializerContext) context;
        return networkSerializerContext.getExtensionVersion(getExtensionName())
                .map(this::versionOf)
                .orElseGet(() -> getVersion(networkSerializerContext.getVersion()));
    }

    @Override
    public void checkReadingCompatibility(DeserializerContext context) {
        NetworkDeserializerContext networkContext = convertContext(context);
        IidmVersion iidmVersion = networkContext.getVersion();
        checkCompatibilityNetworkVersion(iidmVersion);
        if (versions.stream().filter(v -> v.supports(iidmVersion)).noneMatch(v -> networkContext.containsExtensionVersion(getExtensionName(), v.getVersionString()))) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + iidmVersion.toString(".")
                    + ") is not compatible with the " + extensionName + " extension's namespace URI.");
        }
    }

    public boolean checkWritingCompatibility(String extensionVersion, IidmVersion iidmVersion) {
        if (!versionOf(extensionVersion, true).supports(iidmVersion)) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + iidmVersion.toString(".")
                    + ") is not compatible with " + extensionName + " version " + extensionVersion);
        }
        return true;
    }

    private void checkCompatibilityNetworkVersion(IidmVersion version) {
        if (versions.stream().noneMatch(v -> v.supports(version))) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + version.toString(".")
                    + ") is not supported by the " + getExtensionName() + " extension's XML serializer.");
        }
    }

    @Override
    public String getNamespacePrefix() {
        return getDefaultVersion().getNamespacePrefix();
    }

    @Override
    public String getNamespacePrefix(String extensionVersion) {
        return versionOf(extensionVersion, true).getNamespacePrefix();
    }

    @Override
    public void checkExtensionVersionSupported(String extensionVersion) {
        versionOf(extensionVersion, true);
    }

    @Override
    public InputStream getXsdAsStream() {
        return Objects.requireNonNull(getClass().getResourceAsStream(getDefaultVersion().getXsdResourcePath()));
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return versions.stream()
                .map(SerDeVersion::getXsdResourcePath)
                .map(getClass()::getResourceAsStream)
                .map(s -> Objects.requireNonNull(s, "xsd resource not found"))
                .toList();
    }

    protected V getDefaultVersion() {
        return versions.get(versions.size() - 1);
    }

    public V versionOf(String extensionVersion) {
        return versionOf(extensionVersion, false);
    }

    private V versionOf(String extensionVersion, boolean throwIfUnknown) {
        Objects.requireNonNull(extensionVersion);
        var a = versions.stream()
                .filter(v -> extensionVersion.equals(v.getVersionString()))
                .findFirst(); // there can only be 0 or exactly 1 match
        if (throwIfUnknown) {
            return a.orElseThrow(() -> new PowsyblException("The " + extensionName + " extension version " + extensionVersion + " is unknown."));
        } else {
            return a.orElse(null);
        }
    }

    /**
     * Safe conversion of a XmlWriterContext to a NetworkXmlWriterContext
     */
    protected static NetworkSerializerContext convertContext(SerializerContext context) {
        if (context instanceof NetworkSerializerContext networkSerializerContext) {
            return networkSerializerContext;
        }
        throw new IllegalArgumentException("context is not a NetworkXmlWriterContext");
    }

    /**
     * Safe conversion of a XmlReaderContext to a NetworkXmlReaderContext
     */
    protected static NetworkDeserializerContext convertContext(DeserializerContext context) {
        if (context instanceof NetworkDeserializerContext networkDeserializerContext) {
            return networkDeserializerContext;
        }
        throw new IllegalArgumentException("context is not a NetworkXmlReaderContext");
    }
}
