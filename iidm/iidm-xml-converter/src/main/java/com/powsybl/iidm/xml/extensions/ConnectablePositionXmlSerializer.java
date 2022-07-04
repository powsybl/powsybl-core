/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ConnectablePositionXmlSerializer<C extends Connectable<C>> implements ExtensionXmlSerializer<C, ConnectablePosition<C>> {

    @Override
    public String getExtensionName() {
        return  ConnectablePosition.NAME;
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super ConnectablePosition> getExtensionClass() {
        return ConnectablePosition.class;
    }

    @Override
    public boolean hasSubElements() {
        return true;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/connectablePosition.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/connectable_position/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "cp";
    }

    private void writePosition(ConnectablePosition.Feeder feeder, Integer i, XmlWriterContext context) throws XMLStreamException {
        context.getExtensionsWriter().writeEmptyElement(getNamespaceUri(), "feeder" + (i != null ? i : ""));
        context.getExtensionsWriter().writeAttribute("name", feeder.getName());
        Optional<Integer> oOrder = feeder.getOrder();
        if (oOrder.isPresent()) {
            XmlUtil.writeInt("order", oOrder.get(), context.getExtensionsWriter());
        }
        context.getExtensionsWriter().writeAttribute("direction", feeder.getDirection().name());
    }

    @Override
    public void write(ConnectablePosition connectablePosition, XmlWriterContext context) throws XMLStreamException {
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

    private void readPosition(XmlReaderContext context, ConnectablePositionAdder.FeederAdder adder) {
        String name = context.getReader().getAttributeValue(null, "name");
        Optional.ofNullable(XmlUtil.readOptionalIntegerAttribute(context.getReader(), "order")).
                ifPresent(adder::withOrder);
        ConnectablePosition.Direction direction = ConnectablePosition.Direction.valueOf(context.getReader().getAttributeValue(null, "direction"));
        adder.withName(name).withDirection(direction).add();
    }

    @Override
    public ConnectablePosition read(Connectable connectable, XmlReaderContext context) throws XMLStreamException {
        ConnectablePositionAdder adder = ((Connectable<?>) connectable).newExtension(ConnectablePositionAdder.class);
        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {

            switch (context.getReader().getLocalName()) {
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
        adder.add();
        return ((Connectable<?>) connectable).getExtension(ConnectablePosition.class);
    }
}
