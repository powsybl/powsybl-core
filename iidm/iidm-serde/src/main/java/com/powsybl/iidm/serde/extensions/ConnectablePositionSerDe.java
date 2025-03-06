/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkSerializerContext;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ConnectablePositionSerDe<C extends Connectable<C>> extends AbstractVersionableNetworkExtensionSerDe<C, ConnectablePosition<C>, ConnectablePositionSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/connectablePosition_V1_0.xsd", "http://www.itesla_project.eu/schema/iidm/ext/connectable_position/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, null),
        V_1_1("/xsd/connectablePosition_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/connectable_position/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_0, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "cp", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, ConnectablePosition.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public ConnectablePositionSerDe() {
        super(ConnectablePosition.NAME, ConnectablePosition.class, Version.values());
    }

    private void writePosition(String connectableId, ConnectablePosition.Feeder feeder, Integer i, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getExtensionVersion(ConnectablePosition.NAME)
                .map(this::getNamespaceUri)
                .orElseGet(this::getNamespaceUri), "feeder" + (i != null ? i : ""));
        String name = switch (getExtensionVersionToExport(context)) {
            case V_1_0 -> feeder.getName().orElse(connectableId);
            case V_1_1 -> feeder.getName().orElse(null);
        };
        context.getWriter().writeStringAttribute("name", name);
        context.getWriter().writeOptionalIntAttribute("order", feeder.getOrder().orElse(null));
        context.getWriter().writeEnumAttribute("direction", feeder.getDirection());
        context.getWriter().writeEndNode();
    }

    @Override
    public void write(ConnectablePosition<C> connectablePosition, SerializerContext context) {
        NetworkSerializerContext networkContext = convertContext(context);
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
            if (getExtensionVersionImported(context).isLessThan(Version.V_1_1)) {
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
}
