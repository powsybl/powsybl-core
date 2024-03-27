/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.measurements;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(CgmesImportPostProcessor.class)
public class CgmesMeasurementsPostProcessor implements CgmesImportPostProcessor {

    public static final String ANALOG_TYPES_MAPPING = "iidm.import.cgmes.analog-types-mapping";
    public static final String DISCRETE_TYPES_MAPPING = "iidm.import.cgmes.discrete-types-mapping";

    private static final Parameter ANALOG_TYPES_MAPPING_PARAMETER = new Parameter(ANALOG_TYPES_MAPPING,
            ParameterType.STRING_LIST,
            "maps CGMES analog types with IIDM measurement types",
            Collections.emptyList());

    private static final Parameter DISCRETE_TYPES_MAPPING_PARAMETER = new Parameter(DISCRETE_TYPES_MAPPING,
            ParameterType.STRING_LIST,
            "maps CGMES discrete types with IIDM discrete measurement types",
            Collections.emptyList());

    private final ParameterDefaultValueConfig defaultValueConfig;

    public CgmesMeasurementsPostProcessor(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(Objects.requireNonNull(platformConfig));
    }

    public CgmesMeasurementsPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public String getName() {
        return "measurements";
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        CgmesMeasurementsModel model = new CgmesMeasurementsModel(tripleStore);
        PropertyBags bays = model.bays();
        for (PropertyBag analog : model.analogs()) {
            CgmesAnalogPostProcessor.process(network, analog.getId("Analog"), analog.getId("Terminal"),
                    analog.getId("powerSystemResource"),
                    analog.getId("type"),
                    bays,
                    createTypesMapping(Parameter.readStringList("CGMES", null, ANALOG_TYPES_MAPPING_PARAMETER, defaultValueConfig)));
        }
        for (PropertyBag discrete : model.discretes()) {
            CgmesDiscretePostProcessor.process(network, discrete.getId("Discrete"), discrete.getId("Terminal"),
                    discrete.getId("powerSystemResource"),
                    discrete.getId("type"),
                    bays,
                    createTypesMapping(Parameter.readStringList("CGMES", null, DISCRETE_TYPES_MAPPING_PARAMETER, defaultValueConfig)));
        }
    }

    private static Map<String, String> createTypesMapping(List<String> typesMappingList) {
        if (typesMappingList.isEmpty()) {
            return Collections.emptyMap();
        }
        return typesMappingList.stream()
                .collect(Collectors.toMap(s -> s.split(",")[0].replaceAll("\\s+", ""), s -> s.split(",")[1].replaceAll("\\s+", "")));
    }
}
