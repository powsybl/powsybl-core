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
import com.powsybl.iidm.network.test.LoadQuxExt;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;
import com.powsybl.iidm.serde.extensions.SerDeVersion;

import java.io.InputStream;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadQuxSerDe extends AbstractVersionableNetworkExtensionSerDe<Load, LoadQuxExt, LoadQuxSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/V1_0/xsd/loadQux.xsd", "http://www.powsybl.org/schema/iidm/ext/load_qux/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_1);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "lq", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, "loadQux");
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public LoadQuxSerDe() {
        super("loadQux", LoadQuxExt.class, Version.values());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/V1_0/xsd/loadQux.xsd");
    }

    @Override
    public void write(LoadQuxExt extension, SerializerContext context) {
        // do nothing
    }

    @Override
    public LoadQuxExt read(Load extendable, DeserializerContext context) {
        return new LoadQuxExt(extendable);
    }
}
