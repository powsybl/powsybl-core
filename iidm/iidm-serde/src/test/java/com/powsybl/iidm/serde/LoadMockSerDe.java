/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadMockExt;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;
import com.powsybl.iidm.serde.extensions.SerDeVersion;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadMockSerDe extends AbstractVersionableNetworkExtensionSerDe<Load, LoadMockExt, LoadMockSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_0_1("/V1_0/xsd/loadMock_V0_1.xsd", "http://www.powsybl.org/schema/iidm/ext/load_element_mock/1_0",
                new VersionNumbers(0, 1), IidmVersion.V_1_0, IidmVersion.V_1_1, "lmock", "loadElementMock"),
        V_0_2("/V1_0/xsd/loadMock_V0_2.xsd", "http://www.powsybl.org/schema/iidm/ext/load_element_mock/1_1",
                new VersionNumbers(0, 2), IidmVersion.V_1_0, IidmVersion.V_1_1, "lem", "loadEltMock"),
        V_1_0("/V1_0/xsd/loadMock_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/load_mock/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_1),
        V_1_1("/V1_1/xsd/loadMock_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/load_mock/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_1, null),
        V_1_2("/V1_1/xsd/loadMock_V1_2.xsd", "http://www.powsybl.org/schema/iidm/ext/load_mock/1_2",
                new VersionNumbers(1, 2), IidmVersion.V_1_1, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this(xsdResourcePath, namespaceUri, versionNumbers, minIidmVersionIncluded, maxIidmVersionExcluded,
                    "lmock", "loadMock");
        }

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded,
                String namespacePrefix, String serializationName) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, namespacePrefix, versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, serializationName);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public LoadMockSerDe() {
        super("loadMock", LoadMockExt.class, Version.values());
    }

    @Override
    public void write(LoadMockExt extension, SerializerContext context) {
        // empty extension
    }

    @Override
    public LoadMockExt read(Load load, DeserializerContext context) {
        checkReadingCompatibility((NetworkDeserializerContext) context);
        context.getReader().readEndNode();
        var loadMock = new LoadMockExt(load);
        load.addExtension(LoadMockExt.class, loadMock);
        return loadMock;
    }
}
