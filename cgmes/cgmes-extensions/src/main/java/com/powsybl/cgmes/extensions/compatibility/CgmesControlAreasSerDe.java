/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions.compatibility;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesControlAreasSerDe extends AbstractVersionableNetworkExtensionSerDe<Network, CgmesControlAreasSerDe.DummyExt> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesControlAreasSerDe.class);

    private static final String CONTROL_AREA_ROOT_ELEMENT = "controlArea";
    private static final String CONTROL_AREA_ARRAY_ELEMENT = "controlAreas";
    public static final String TERMINAL_ROOT_ELEMENT = "terminal";
    public static final String TERMINAL_ARRAY_ELEMENT = "terminals";
    public static final String BOUNDARY_ROOT_ELEMENT = "boundary";
    public static final String BOUNDARY_ARRAY_ELEMENT = "boundaries";

    public CgmesControlAreasSerDe() {
        super("cgmesControlAreas", CgmesControlAreasSerDe.DummyExt.class, "cca",
                new ImmutableMap.Builder<IidmVersion, ImmutableSortedSet<String>>()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_2, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_3, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_4, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_5, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_6, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_7, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_8, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_9, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_10, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_11, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_12, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_13, ImmutableSortedSet.of("1.0"))
                        .build(),
                new ImmutableMap.Builder<String, String>()
                        .put("1.0", "http://www.powsybl.org/schema/iidm/ext/cgmes_control_areas/1_0")
                        .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/cgmesControlAreas.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(Objects.requireNonNull(getClass().getResourceAsStream("/xsd/cgmesControlAreas.xsd")));
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(CONTROL_AREA_ARRAY_ELEMENT, CONTROL_AREA_ROOT_ELEMENT,
                TERMINAL_ARRAY_ELEMENT, TERMINAL_ROOT_ELEMENT,
                BOUNDARY_ARRAY_ELEMENT, BOUNDARY_ROOT_ELEMENT);
    }

    @Override
    public void write(DummyExt extension, SerializerContext context) {
        throw new UnsupportedOperationException("Backward writing compatibility is not yet supported.");
    }

    @Override
    public DummyExt read(Network extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        TreeDataReader reader = networkContext.getReader();
        reader.readChildNodes(elementName -> {
            if (elementName.equals(CONTROL_AREA_ROOT_ELEMENT)) {
                String id = reader.readStringAttribute("id");
                if (extendable.getArea(id) == null) {
                    Area area = extendable.newArea()
                            .setAreaType(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE)
                            .setId(id)
                            .setName(reader.readStringAttribute("name"))
                            .setInterchangeTarget(reader.readDoubleAttribute("netInterchange"))
                            .add();

                    OptionalDouble pTolerance = reader.readOptionalDoubleAttribute("pTolerance");
                    pTolerance.ifPresent(t -> area.setProperty(CgmesNames.P_TOLERANCE, Double.toString(t)));

                    String energyIdentificationCodeEic = reader.readStringAttribute("energyIdentificationCodeEic");
                    if (!Strings.isNullOrEmpty(energyIdentificationCodeEic)) {
                        area.addAlias(energyIdentificationCodeEic, CgmesNames.ENERGY_IDENT_CODE_EIC);
                    }
                    readBoundariesAndTerminals(networkContext, area, extendable);
                } else {
                    LOGGER.warn("Area with id {} already exists. Skipping this CgmesControlArea.", id);
                    reader.skipChildNodes();
                }
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'cgmesControlArea'");
            }
        });
        return null;
    }

    private void readBoundariesAndTerminals(NetworkDeserializerContext networkContext, Area area, Network network) {
        TreeDataReader reader = networkContext.getReader();
        reader.readChildNodes(elementName -> {
            switch (elementName) {
                case BOUNDARY_ROOT_ELEMENT -> {
                    String id = networkContext.getAnonymizer().deanonymizeString(reader.readStringAttribute("id"));
                    Identifiable<?> identifiable = network.getIdentifiable(id);
                    boolean isAc = true;  // Set to "true" because this piece of data is not available
                    if (identifiable instanceof DanglingLine dl) {
                        area.newAreaBoundary()
                                .setAc(isAc)
                                .setBoundary(dl.getBoundary())
                                .add();
                    } else if (identifiable instanceof TieLine tl) {
                        TwoSides side = reader.readEnumAttribute("side", TwoSides.class);
                        area.newAreaBoundary()
                                .setAc(isAc)
                                .setBoundary(tl.getDanglingLine(side).getBoundary())
                                .add();
                    } else {
                        throw new PowsyblException("Unexpected Identifiable instance: " + identifiable.getClass());
                    }
                    reader.readEndNode();
                }
                case TERMINAL_ROOT_ELEMENT -> {
                    Terminal terminal = TerminalRefSerDe.readTerminal(networkContext, network);
                    area.newAreaBoundary()
                        .setAc(terminal.getConnectable().getType() != IdentifiableType.HVDC_CONVERTER_STATION)
                        .setTerminal(terminal)
                        .add();
                }
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'controlArea'");
            }
        });
    }

    @Override
    public boolean isSerializable(CgmesControlAreasSerDe.DummyExt ext) {
        return false; // Backward writing compatibility is not yet supported.
    }

    // An extension is needed to define the SerDe. But it shouldn't be used otherwise.
    public static class DummyExt implements Extension<Network> {
        @Override
        public String getName() {
            return "cgmesControlAreas";
        }

        @Override
        public Network getExtendable() {
            throw new UnsupportedOperationException("This extension should not be used.");
        }

        @Override
        public void setExtendable(Network extendable) {
            throw new UnsupportedOperationException("This extension should not be used.");
        }
    }
}
