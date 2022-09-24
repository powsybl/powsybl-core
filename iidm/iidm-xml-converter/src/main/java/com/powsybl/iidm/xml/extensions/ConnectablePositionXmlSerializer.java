/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ConnectablePositionXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C, ConnectablePosition<C>> {

    public ConnectablePositionXmlSerializer() {
        super(ConnectablePosition.NAME, "network", ConnectablePosition.class, true,
                "connectablePosition.xsd", "http://www.itesla_project.eu/schema/iidm/ext/connectable_position/1_0",
                "cp");
    }

    private void writePosition(ConnectablePosition.Feeder feeder, Integer i, XmlWriterContext context) {
        context.getWriter().writeEmptyNode(getNamespaceUri(), "feeder" + (i != null ? i : ""));
        context.getWriter().writeStringAttribute("name", feeder.getName());
        Optional<Integer> oOrder = feeder.getOrder();
        oOrder.ifPresent(integer -> context.getWriter().writeIntAttribute("order", integer));
        context.getWriter().writeStringAttribute("direction", feeder.getDirection().name());
    }

    @Override
    public void write(ConnectablePosition connectablePosition, XmlWriterContext context) {
        if (connectablePosition.getFeeder() != null) {
            writePosition(connectablePosition.getFeeder(), null, context);
        }
        if (connectablePosition.getFeeder1() != null) {
            writePosition(connectablePosition.getFeeder1(), 1, context);
        }
        if (connectablePosition.getFeeder2() != null) {
            writePosition(connectablePosition.getFeeder2(), 2, context);
        }
        if (connectablePosition.getFeeder3() != null) {
            writePosition(connectablePosition.getFeeder3(), 3, context);
        }
    }

    private void readPosition(XmlReaderContext context, ConnectablePositionAdder.FeederAdder<C> adder) {
        String name = context.getReader().readStringAttribute("name");
        Optional.ofNullable(context.getReader().readIntAttribute("order")).
                ifPresent(adder::withOrder);
        ConnectablePosition.Direction direction = context.getReader().readEnumAttribute("direction", ConnectablePosition.Direction.class);
        adder.withName(name).withDirection(direction).add();
    }

    @Override
    public ConnectablePosition read(Connectable connectable, XmlReaderContext context) {
        ConnectablePositionAdder<C> adder = ((Connectable<?>) connectable).newExtension(ConnectablePositionAdder.class);
        context.getReader().readUntilEndNode(getExtensionName(), () -> {

            switch (context.getReader().getNodeName()) {
                case "feeder":
                    readPosition(context, adder.newFeeder());
                    break;

                case "feeder1":
                    readPosition(context, adder.newFeeder1());
                    break;

                case "feeder2":
                    readPosition(context, adder.newFeeder2());
                    break;

                case "feeder3":
                    readPosition(context, adder.newFeeder3());
                    break;

                default:
                    throw new AssertionError();
            }
        });
        return adder.add();
    }
}
