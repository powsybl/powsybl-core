/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractVersionableNetworkExtensionXmlSerializer<T extends Extendable, E extends Extension<T>> implements ExtensionXmlSerializer<T, E> {

    private static final String INCOMPATIBILITY_NETWORK_VERSION_MESSAGE = "IIDM-XML version of network (";

    private final String extensionName;
    private final Class<? super E> extensionClass;
    private final boolean subElements;
    private final String namespacePrefix;
    private final Map<IidmXmlVersion, ImmutableSortedSet<String>> extensionVersions = new EnumMap<>(IidmXmlVersion.class);
    private final Map<String, String> namespaceUris = new HashMap<>();

    protected AbstractVersionableNetworkExtensionXmlSerializer(String extensionName, Class<? super E> extensionClass, boolean subElements, String namespacePrefix,
                                                               Map<IidmXmlVersion, ImmutableSortedSet<String>> extensionVersions, Map<String, String> namespaceUris) {
        this.extensionName = Objects.requireNonNull(extensionName);
        this.extensionClass = Objects.requireNonNull(extensionClass);
        this.subElements = subElements;
        this.namespacePrefix = Objects.requireNonNull(namespacePrefix);
        this.extensionVersions.putAll(Objects.requireNonNull(extensionVersions));
        this.namespaceUris.putAll(Objects.requireNonNull(namespaceUris));
    }

    @Override
    public String getExtensionName() {
        return extensionName;
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
    public boolean hasSubElements() {
        return subElements;
    }

    @Override
    public String getNamespaceUri() {
        return getNamespaceUri(getVersion());
    }

    @Override
    public String getNamespaceUri(String extensionVersion) {
        return Optional.ofNullable(namespaceUris.get(extensionVersion))
                .orElseThrow(() -> new PowsyblException("Namespace URI null for " + getExtensionName() +
                        " extension's version " + extensionVersion));
    }

    @Override
    public String getVersion() {
        return getVersion(IidmXmlConstants.CURRENT_IIDM_XML_VERSION);
    }

    public boolean versionExists(IidmXmlVersion networkVersion) {
        return extensionVersions.containsKey(networkVersion);
    }

    /**
     * get the oldest version of an extension working with a network version.
     *
     * @param networkVersion
     * @return
     */
    public String getVersion(IidmXmlVersion networkVersion) {
        return extensionVersions.get(networkVersion).last();
    }

    @Override
    public Set<String> getVersions() {
        return namespaceUris.keySet();
    }

    protected void checkReadingCompatibility(NetworkXmlReaderContext networkContext) {
        IidmXmlVersion version = networkContext.getVersion();
        checkCompatibilityNetworkVersion(version);
        if (extensionVersions.get(version).stream().noneMatch(v -> networkContext.containsExtensionNamespaceUri(getNamespaceUri(v)))) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + version.toString(".")
                    + ") is not compatible with the " + extensionName + " extension's namespace URI.");
        }
    }

    public void checkWritingCompatibility(String extensionVersion, IidmXmlVersion version) {
        checkExtensionVersionSupported(extensionVersion);
        checkCompatibilityNetworkVersion(version);
        if (!extensionVersions.get(version).contains(extensionVersion)) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + version.toString(".")
                    + ") is not compatible with the version " + extensionVersion + " of the " + extensionName + " extension.");
        }
    }

    private void checkCompatibilityNetworkVersion(IidmXmlVersion version) {
        if (!extensionVersions.containsKey(version)) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + version.toString(".")
                    + ") is not supported by the " + getExtensionName() + " extension's XML serializer.");
        }
    }

    @Override
    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    @Override
    public void checkExtensionVersionSupported(String extensionVersion) {
        if (!namespaceUris.containsKey(extensionVersion)) {
            throw new PowsyblException("The version " + extensionVersion + " of the " + extensionName + " extension is not supported.");
        }
    }
}
