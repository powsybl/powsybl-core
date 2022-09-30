/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.sensitivity.SensitivityAnalysisProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public final class JsonSensitivityAnalysisParameters {

    public static Map<String, ExtensionJsonSerializer> getExtensionSerializers() {
        List<SensitivityAnalysisProvider> providers = new ServiceLoaderCache<>(SensitivityAnalysisProvider.class).getServices();
        return providers.stream()
                .flatMap(securityAnalysisProvider -> securityAnalysisProvider.getSpecificParametersSerializer().stream())
                .collect(Collectors.toMap(ExtensionProvider::getExtensionName,
                    securityAnalysisProvider -> securityAnalysisProvider));
    }

    private JsonSensitivityAnalysisParameters() {
    }

    public static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new SensitivityJsonModule());
    }
}
