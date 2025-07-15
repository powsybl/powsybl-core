/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.io.InputStream;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ConnectablePositionSerDe<C extends Connectable<C>> extends AbstractVersionableNetworkExtensionSerDe<C, ConnectablePosition<C>> {

    private static final String V_1_0 = "1.0";
    private static final String V_1_1 = "1.1";

    public ConnectablePositionSerDe() {
        super(ConnectablePosition.NAME, ConnectablePosition.class, "cp",
                ImmutableMap.<IidmVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_2, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_3, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_4, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_5, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_6, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_7, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_8, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_9, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_10, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_11, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_12, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_13, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmVersion.V_1_14, ImmutableSortedSet.of(V_1_1))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put(V_1_0, "http://www.itesla_project.eu/schema/iidm/ext/connectable_position/1_0")
                        .put(V_1_1, "http://www.powsybl.org/schema/iidm/ext/connectable_position/1_1")
                        .build());
    }

    private void writePosition(String connectableId, ConnectablePosition.Feeder feeder, Integer i, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getExtensionVersion(ConnectablePosition.NAME)
                .map(this::getNamespaceUri)
                .orElseGet(this::getNamespaceUri), "feeder" + (i != null ? i : ""));
        String extVersionStr = context.getExtensionVersion(ConnectablePosition.NAME)
                .orElseGet(() -> getVersion(context.getVersion()));
        switch (extVersionStr) {
            case V_1_0:
                context.getWriter().writeStringAttribute("name", feeder.getName().orElse(connectableId));
                break;
            case V_1_1:
                context.getWriter().writeStringAttribute("name", feeder.getName().orElse(null));
                break;
            default:
                throw new PowsyblException("Unsupported version (" + extVersionStr + ") for " + ConnectablePosition.NAME);
        }
        context.getWriter().writeOptionalIntAttribute("order", feeder.getOrder().orElse(null));
        context.getWriter().writeEnumAttribute("direction", feeder.getDirection());
        context.getWriter().writeEndNode();
    }

    @Override
    public void write(ConnectablePosition<C> connectablePosition, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        String connectableId = connectablePosition.getExtendable().getId();
        if (connectablePosition.getFeeder() != null) {
            writePosition(connectableId, connectablePosition.getFeeder(), null, networkContext);
        }
        if (connectablePosition.getFeeder1() != null) {
            writePosition(connectableId, connectablePosition.getFeeder1(), 1, networkContext);
        }
        if (connectablePosition.getFeeder2() != null) {
            writePosition(connectableId, connectablePosition.getFeeder2(), 2, networkContext);
        }
        if (connectablePosition.getFeeder3() != null) {
            writePosition(connectableId, connectablePosition.getFeeder3(), 3, networkContext);
        }
    }

    private void readPosition(DeserializerContext context, ConnectablePositionAdder.FeederAdder<C> adder) {
        String name = context.getReader().readStringAttribute("name");
        context.getReader().readOptionalIntAttribute("order")
                .ifPresent(adder::withOrder);
        ConnectablePosition.Direction direction = context.getReader().readEnumAttribute("direction", ConnectablePosition.Direction.class);
        context.getReader().readEndNode();
        if (name != null) {
            adder.withName(name);
        } else {
            NetworkDeserializerContext networkSerializerReaderContext = (NetworkDeserializerContext) context;
            String extensionVersionStr = networkSerializerReaderContext.getExtensionVersion(this).orElseThrow(IllegalStateException::new);
            if (V_1_1.compareTo(extensionVersionStr) > 0) {
                throw new PowsyblException("Feeder name is mandatory for version < 1.1");
            }
        }
        adder.withDirection(direction).add();
    }

    @Override
    public ConnectablePosition<C> read(C connectable, DeserializerContext context) {
        ConnectablePositionAdder<C> adder = connectable.newExtension(ConnectablePositionAdder.class);
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case "feeder" -> readPosition(context, adder.newFeeder());
                case "feeder1" -> readPosition(context, adder.newFeeder1());
                case "feeder2" -> readPosition(context, adder.newFeeder2());
                case "feeder3" -> readPosition(context, adder.newFeeder3());
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'position'");
            }
        });
        return adder.add();
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/connectablePosition_V1_1.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/connectablePosition_V1_1.xsd"),
                getClass().getResourceAsStream("/xsd/connectablePosition_V1_0.xsd"));
    }
}
