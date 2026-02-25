/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.powsybl.iidm.serde.util.IidmSerDeUtil.fromMinimumVersionOrElse;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class BaseVoltageMappingSerDe extends AbstractExtensionSerDe<Network, BaseVoltageMapping> {

    public static final String BASE_VOLTAGE_ARRAY_ELEMENT = "baseVoltages";
    public static final String BASE_VOLTAGE_ROOT_ELEMENT = "baseVoltage";

    public BaseVoltageMappingSerDe() {
        super(BaseVoltageMapping.NAME, "network", BaseVoltageMapping.class, "baseVoltageMapping.xsd",
                "http://www.powsybl.org/schema/iidm/ext/base_voltage_mapping/1_0", "bv");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(BASE_VOLTAGE_ARRAY_ELEMENT, BASE_VOLTAGE_ROOT_ELEMENT);
    }

    @Override
    public void write(BaseVoltageMapping extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        Map<Double, BaseVoltageMapping.BaseVoltageSource> sortedBaseVoltages = extension.getBaseVoltages().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        writer.writeStartNodes();
        sortedBaseVoltages.forEach((nominalV, baseVoltageSource) -> {
            writer.writeStartNode(getNamespaceUri(), BASE_VOLTAGE_ROOT_ELEMENT);
            writer.writeDoubleAttribute("nominalVoltage", nominalV);
            writer.writeEnumAttribute("source", baseVoltageSource.getSource());
            writer.writeStringAttribute("id", fromMinimumVersionOrElse(IidmVersion.V_1_16, networkContext,
                    () -> networkContext.getAnonymizer().anonymizeString(baseVoltageSource.getId()),
                    baseVoltageSource::getId));
            writer.writeEndNode();
        });
        writer.writeEndNodes();
    }

    @Override
    public BaseVoltageMapping read(Network extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        TreeDataReader reader = networkContext.getReader();
        BaseVoltageMappingAdder mappingAdder = extendable.newExtension(BaseVoltageMappingAdder.class);
        mappingAdder.add();
        BaseVoltageMapping mapping = extendable.getExtension(BaseVoltageMapping.class);
        reader.readChildNodes(elementName -> {
            if (elementName.equals(BASE_VOLTAGE_ROOT_ELEMENT)) {
                double nominalV = reader.readDoubleAttribute("nominalVoltage");
                Source sourceBV = reader.readEnumAttribute("source", Source.class);
                String baseVoltageId = fromMinimumVersionOrElse(IidmVersion.V_1_16, networkContext,
                        () -> networkContext.getAnonymizer().deanonymizeString(reader.readStringAttribute("id")),
                        () -> reader.readStringAttribute("id"));
                reader.readEndNode();
                mapping.addBaseVoltage(nominalV, baseVoltageId, sourceBV);
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'baseVoltageMapping'");
            }
        });
        return mapping;
    }

    /**
     * A {@link BaseVoltageMapping} extension is serializable if it contains at least one base voltage
     *
     * @param extension The extension to check
     * @return true if it contains at least one link, false if not
     */
    @Override
    public boolean isSerializable(BaseVoltageMapping extension) {
        return !extension.isBaseVoltageEmpty();
    }
}
