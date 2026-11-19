/**
 * Copyright (c) 2026, Artelys (https://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extensions of {@link SensitivityAnalysisParameters} which are not the specific parameters of any provider.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class SensitivityAnalysisParametersNonProviderExtensionTest {

    @Test
    void jsonRoundTrip() throws IOException {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.addExtension(BusinessExtension.class, new BusinessExtension().setValue("foo"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonSensitivityAnalysisParameters.createObjectMapper().writeValue(os, parameters);

        SensitivityAnalysisParameters read = JsonSensitivityAnalysisParameters.read(new ByteArrayInputStream(os.toByteArray()));
        assertEquals("foo", read.getExtension(BusinessExtension.class).getValue());
    }

    @Test
    void loadFromConfig() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            assertNull(SensitivityAnalysisParameters.load(platformConfig).getExtension(BusinessExtension.class));

            platformConfig.createModuleConfig("business-extension").setStringProperty("value", "foo");
            assertEquals("foo", SensitivityAnalysisParameters.load(platformConfig).getExtension(BusinessExtension.class).getValue());
        }
    }

    public static class BusinessExtension extends AbstractExtension<SensitivityAnalysisParameters> {

        private String value = "default";

        @Override
        public String getName() {
            return "business-extension";
        }

        public String getValue() {
            return value;
        }

        public BusinessExtension setValue(String value) {
            this.value = value;
            return this;
        }
    }

    @AutoService(ExtensionJsonSerializer.class)
    public static class BusinessExtensionSerializer implements ExtensionJsonSerializer<SensitivityAnalysisParameters, BusinessExtension> {

        @Override
        public void serialize(BusinessExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("value", extension.getValue());
            jsonGenerator.writeEndObject();
        }

        @Override
        public BusinessExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            BusinessExtension extension = new BusinessExtension();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                if ("value".equals(jsonParser.currentName())) {
                    extension.setValue(jsonParser.nextTextValue());
                }
            }
            return extension;
        }

        @Override
        public String getExtensionName() {
            return "business-extension";
        }

        @Override
        public String getCategoryName() {
            return SensitivityAnalysisParameters.EXTENSIONS_CATEGORY;
        }

        @Override
        public Class<? super BusinessExtension> getExtensionClass() {
            return BusinessExtension.class;
        }
    }

    @AutoService(ExtensionConfigLoader.class)
    public static class BusinessExtensionConfigLoader implements ExtensionConfigLoader<SensitivityAnalysisParameters, BusinessExtension> {

        @Override
        public BusinessExtension load(PlatformConfig platformConfig) {
            return platformConfig.getOptionalModuleConfig("business-extension")
                    .map(config -> new BusinessExtension().setValue(config.getStringProperty("value")))
                    .orElse(null);
        }

        @Override
        public String getExtensionName() {
            return "business-extension";
        }

        @Override
        public String getCategoryName() {
            return SensitivityAnalysisParameters.EXTENSIONS_CATEGORY;
        }

        @Override
        public Class<? super BusinessExtension> getExtensionClass() {
            return BusinessExtension.class;
        }
    }
}
