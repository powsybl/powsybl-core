/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;

import java.util.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractVersionableNetworkExtensionSerDe<T extends Extendable, E extends Extension<T>> implements ExtensionSerDe<T, E> {

    private static final String INCOMPATIBILITY_NETWORK_VERSION_MESSAGE = "IIDM version of network (";

    private final String extensionName;
    private final Class<? super E> extensionClass;
    private final String namespacePrefix;
    private final Map<IidmVersion, ImmutableSortedSet<String>> extensionVersions = new EnumMap<>(IidmVersion.class);
    private final BiMap<String, String> namespaceUris = HashBiMap.create();
    private final Map<String, String> serializationNameByVersion = new HashMap<>();

    protected AbstractVersionableNetworkExtensionSerDe(String extensionName, Class<? super E> extensionClass, String namespacePrefix,
                                                       Map<IidmVersion, ImmutableSortedSet<String>> extensionVersions, Map<String, String> namespaceUris) {
        this(extensionName, extensionClass, namespacePrefix, extensionVersions, namespaceUris, null);
    }

    protected AbstractVersionableNetworkExtensionSerDe(String extensionName, Class<? super E> extensionClass, String namespacePrefix,
                                                       Map<IidmVersion, ImmutableSortedSet<String>> extensionVersions,
                                                       Map<String, String> namespaceUris,
                                                       Map<String, Set<String>> versionsBySerializationName) {
        this.extensionName = Objects.requireNonNull(extensionName);
        this.extensionClass = Objects.requireNonNull(extensionClass);
        this.namespacePrefix = Objects.requireNonNull(namespacePrefix);
        this.extensionVersions.putAll(Objects.requireNonNull(extensionVersions));
        this.namespaceUris.putAll(Objects.requireNonNull(namespaceUris));
        if (versionsBySerializationName != null) {
            for (Map.Entry<String, Set<String>> entry : versionsBySerializationName.entrySet()) {
                entry.getValue().forEach(version -> this.serializationNameByVersion.put(version, entry.getKey()));
            }
        }
    }

    @Override
    public String getExtensionName() {
        return extensionName;
    }

    @Override
    public String getSerializationName(String extensionVersion) {
        return serializationNameByVersion.getOrDefault(extensionVersion, extensionName);
    }

    @Override
    public Set<String> getSerializationNames() {
        Set<String> versions = new HashSet<>(serializationNameByVersion.values());
        versions.add(extensionName); // The extension name may not be stored in serializationNameByVersion since it's the default name
        return versions;
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
    public String getNamespaceUri(String extensionVersion) {
        return Optional.ofNullable(namespaceUris.get(extensionVersion))
                .orElseThrow(() -> new PowsyblException("Namespace URI null for " + getExtensionName() +
                        " extension's version " + extensionVersion));
    }

    @Override
    public String getVersion() {
        return getVersion(IidmSerDeConstants.CURRENT_IIDM_VERSION);
    }

    public boolean versionExists(IidmVersion networkVersion) {
        return extensionVersions.containsKey(networkVersion);
    }

    /**
     * Get the oldest version of an extension working with a network version.
     */
    public String getVersion(IidmVersion networkVersion) {
        return extensionVersions.get(networkVersion).last();
    }

    @Override
    public String getVersion(String namespaceUri) {
        return Optional.ofNullable(namespaceUris.inverse().get(namespaceUri))
                .orElseThrow(() -> new PowsyblException("The namespace URI " + namespaceUri + " of the " + extensionName + " extension is not supported."));
    }

    @Override
    public Set<String> getVersions() {
        return namespaceUris.keySet();
    }

    protected void checkReadingCompatibility(NetworkDeserializerContext networkContext) {
        IidmVersion version = networkContext.getVersion();
        checkCompatibilityNetworkVersion(version);
        if (extensionVersions.get(version).stream().noneMatch(v -> networkContext.containsExtensionVersion(getExtensionName(), v))) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + version.toString(".")
                    + ") is not compatible with the " + extensionName + " extension's namespace URI.");
        }
    }

    public boolean checkWritingCompatibility(String extensionVersion, IidmVersion version) {
        checkExtensionVersionSupported(extensionVersion);
        checkCompatibilityNetworkVersion(version);
        if (!extensionVersions.get(version).contains(extensionVersion)) {
            throw new PowsyblException(INCOMPATIBILITY_NETWORK_VERSION_MESSAGE + version.toString(".")
                    + ") is not compatible with " + extensionName + " version " + extensionVersion);
        }
        return true;
    }

    private void checkCompatibilityNetworkVersion(IidmVersion version) {
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
            throw new PowsyblException("The " + extensionName + " extension version " + extensionVersion + " is not supported.");
        }
    }
}
