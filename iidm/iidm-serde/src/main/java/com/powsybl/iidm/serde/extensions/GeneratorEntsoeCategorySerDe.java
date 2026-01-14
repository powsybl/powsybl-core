/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;
import com.powsybl.iidm.serde.IidmVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class GeneratorEntsoeCategorySerDe extends AbstractVersionableNetworkExtensionSerDe<Generator, GeneratorEntsoeCategory, GeneratorEntsoeCategorySerDe.Version> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorEntsoeCategorySerDe.class);

    public enum Version implements SerDeVersion<GeneratorEntsoeCategorySerDe.Version> {
        V_1_0("/xsd/generatorEntsoeCategory_V1_0.xsd", "http://www.itesla_project.eu/schema/iidm/ext/generator_entsoe_category/1_0",
            new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_16),
        V_1_1("/xsd/generatorEntsoeCategory_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/generator_entsoe_category/1_1",
            new VersionNumbers(1, 1), IidmVersion.V_1_16, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "gec", versionNumbers,
                minIidmVersionIncluded, maxIidmVersionExcluded, GeneratorEntsoeCategory.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public GeneratorEntsoeCategorySerDe() {
        super("entsoeCategory", GeneratorEntsoeCategory.class, Version.values());
    }

    @Override
    public boolean isValid(GeneratorEntsoeCategory entsoeCategory, SerializerContext context) {
        if (entsoeCategory.getCode() == 0 && getExtensionVersionToExport(context) == Version.V_1_0) {
            LOGGER.warn("Extension entsoeCategory not valid for Generator: {}. Reason: code = 0 not allowed in version 1.0.",
                entsoeCategory.getExtendable().getId());
            return false;
        }
        return true;
    }

    @Override
    public void write(GeneratorEntsoeCategory entsoeCategory, SerializerContext context) {
        context.getWriter().writeNodeContent(Integer.toString(entsoeCategory.getCode()));
    }

    @Override
    public GeneratorEntsoeCategory read(Generator generator, DeserializerContext context) {
        int code = Integer.parseInt(context.getReader().readContent());
        return generator.newExtension(GeneratorEntsoeCategoryAdder.class)
                .withCode(code)
                .add();
    }
}
