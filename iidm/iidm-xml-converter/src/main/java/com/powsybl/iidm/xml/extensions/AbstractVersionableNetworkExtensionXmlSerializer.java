/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractVersionableNetworkExtensionXmlSerializer<T extends Extendable, E extends Extension<T>> extends AbstractExtensionXmlSerializer<T, E> implements NetworkExtensionXmlSerializer<T, E> {

    private final ImmutableMap<IidmXmlVersion, ImmutableSortedSet<String>> extensionVersions;

    protected AbstractVersionableNetworkExtensionXmlSerializer(String extensionName, Class<? super E> extensionClass, boolean subElements, String namespacePrefix,
                                                               ImmutableMap<IidmXmlVersion, ImmutableSortedSet<String>> extensionVersions) {
        super(extensionName, "network", extensionClass, subElements, namespacePrefix);
        this.extensionVersions = extensionVersions;
    }

    @Override
    public String getNamespaceUri() {
        return getNamespaceUri(getVersion());
    }

    @Override
    public String getVersion() {
        return getVersion(IidmXmlConstants.CURRENT_IIDM_XML_VERSION);
    }

    @Override
    public String getVersion(IidmXmlVersion version) {
        return extensionVersions.get(version).last();
    }

    protected void checkReadingCompatibility(NetworkXmlReaderContext networkContext) {
        IidmXmlVersion version = networkContext.getVersion();
        if (!extensionVersions.containsKey(version)) {
            throw new PowsyblException("IIDM-XML version of network (" + version.toString(".")
                    + ") is not supported by the " + getExtensionName() + "extension's XML serializer.");
        }
        if (extensionVersions.get(version).stream().noneMatch(v -> networkContext.containsExtensionNamespaceUri(getNamespaceUri(v)))) {
            throw new PowsyblException("IIDM-XML version of network (" + version.toString(".")
                    + ") is not compatible with the " + getExtensionName() + " extension's namespace URI.");
        }
    }
}
