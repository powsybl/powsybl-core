/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import eu.itesla_project.iidm.network.*;
import javanet.staxutils.IndentingXMLStreamWriter;
import org.joda.time.DateTime;

import javax.xml.stream.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXml implements XmlConstants {

    static final String NETWORK_ROOT_ELEMENT_NAME = "network";

    // cache XMLOutputFactory to improve performance
    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    public static void write(Network n, Path xmlFile) throws XMLStreamException, IOException {
        write(n, new XMLExportOptions(), xmlFile);
    }

    public static void write(Network n, XMLExportOptions options, Path xmlFile) throws XMLStreamException, IOException {
        try (OutputStream os = Files.newOutputStream(xmlFile)) {
            write(n, options, os);
        }
    }

    public static void write(Network n, XMLExportOptions options, OutputStream os) throws XMLStreamException {
        XMLStreamWriter writer = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(os, StandardCharsets.UTF_8.toString());
        if (options.isIndent()) {
            IndentingXMLStreamWriter indentingWriter = new IndentingXMLStreamWriter(writer);
            indentingWriter.setIndent(INDENT);
            writer = indentingWriter;
        }
        writer.writeStartDocument(StandardCharsets.UTF_8.toString(), "1.0");
        writer.setPrefix(IIDM_PREFIX, IIDM_URI);
        writer.writeStartElement(IIDM_URI, NETWORK_ROOT_ELEMENT_NAME);
        writer.writeNamespace(IIDM_PREFIX, IIDM_URI);
        writer.writeAttribute("id", n.getId());
        writer.writeAttribute("caseDate", n.getCaseDate().toString());
        writer.writeAttribute("forecastDistance", Integer.toString(n.getForecastDistance()));
        writer.writeAttribute("sourceFormat", n.getSourceFormat());
        BusFilter filter = BusFilter.create(n, options);
        XmlWriterContext context = new XmlWriterContext(writer, options, filter);
        for (Substation s : n.getSubstations()) {
            SubstationXml.INSTANCE.write(s, null, context);
        }
        for (Line l : n.getLines()) {
            if (!filter.test(l)) {
                continue;
            }
            if (l.isTieLine()) {
                TieLineXml.INSTANCE.write((TieLine) l, n, context);
            } else {
                LineXml.INSTANCE.write(l, n, context);
            }
        }
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    public static Network read(Path xmlFile) throws XMLStreamException, IOException {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            return read(is);
        }
    }

    public static Network read(InputStream is) throws XMLStreamException {
        XMLStreamReader reader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
        reader.next();

        String id = reader.getAttributeValue(null, "id");
        DateTime date = DateTime.parse(reader.getAttributeValue(null, "caseDate"));
        int forecastDistance = XmlUtil.readOptionalIntegerAttributeValue(reader, "forecastDistance", 0);
        String sourceFormat = reader.getAttributeValue(null, "sourceFormat");

        Network network = NetworkFactory.create(id, sourceFormat);
        network.setCaseDate(date);
        network.setForecastDistance(forecastDistance);

        List<Runnable> endTasks = new ArrayList<>();

        XmlUtil.readUntilEndElement(NETWORK_ROOT_ELEMENT_NAME, reader, () -> {
            switch (reader.getLocalName()) {
                case SubstationXml.ROOT_ELEMENT_NAME:
                    SubstationXml.INSTANCE.read(reader, network, endTasks);
                    break;

                case LineXml.ROOT_ELEMENT_NAME:
                    LineXml.INSTANCE.read(reader, network, endTasks);
                    break;

                case TieLineXml.ROOT_ELEMENT_NAME:
                    TieLineXml.INSTANCE.read(reader, network, endTasks);
                    break;

                default:
                    throw new AssertionError();
            }
        });

        endTasks.forEach(Runnable::run);

        return network;
    }

    public static void update(Network network, Path xmlFile) throws XMLStreamException, IOException {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            update(network, is);
        }
    }

    public static void update(Network network, InputStream is) throws XMLStreamException {
        XMLStreamReader reader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
        reader.next();

        final VoltageLevel[] vl = new VoltageLevel[1];

        XmlUtil.readUntilEndElement(NETWORK_ROOT_ELEMENT_NAME, reader, () -> {

            switch (reader.getLocalName()) {
                case VoltageLevelXml.ROOT_ELEMENT_NAME: {
                    String id = reader.getAttributeValue(null, "id");
                    vl[0] = network.getVoltageLevel(id);
                    if (vl[0] == null) {
                        throw new RuntimeException("Voltage level '" + id + "' not found");
                    }
                    break;
                }

                case BusXml.ROOT_ELEMENT_NAME: {
                    String id = reader.getAttributeValue(null, "id");
                    float v = XmlUtil.readFloatAttribute(reader, "v");
                    float angle = XmlUtil.readFloatAttribute(reader, "angle");
                    Bus b = vl[0].getBusBreakerView().getBus(id);
                    if (b == null) {
                        b = vl[0].getBusView().getBus(id);
                    }
                    b.setV(v > 0 ? v : Float.NaN).setAngle(angle);
                    break;
                }

                case GeneratorXml.ROOT_ELEMENT_NAME:
                case LoadXml.ROOT_ELEMENT_NAME:
                case ShuntXml.ROOT_ELEMENT_NAME:
                case DanglingLineXml.ROOT_ELEMENT_NAME: {
                    String id = reader.getAttributeValue(null, "id");
                    float p = XmlUtil.readOptionalFloatAttribute(reader, "p");
                    float q = XmlUtil.readOptionalFloatAttribute(reader, "q");
                    SingleTerminalConnectable inj = (SingleTerminalConnectable) network.getIdentifiable(id);
                    inj.getTerminal().setP(p).setQ(q);
                    break;
                }

                case LineXml.ROOT_ELEMENT_NAME:
                case TwoWindingsTransformerXml.ROOT_ELEMENT_NAME: {
                    String id = reader.getAttributeValue(null, "id");
                    float p1 = XmlUtil.readOptionalFloatAttribute(reader, "p1");
                    float q1 = XmlUtil.readOptionalFloatAttribute(reader, "q1");
                    float p2 = XmlUtil.readOptionalFloatAttribute(reader, "p2");
                    float q2 = XmlUtil.readOptionalFloatAttribute(reader, "q2");
                    TwoTerminalsConnectable branch = (TwoTerminalsConnectable) network.getIdentifiable(id);
                    branch.getTerminal1().setP(p1).setQ(q1);
                    branch.getTerminal2().setP(p2).setQ(q2);
                    break;
                }

                case ThreeWindingsTransformerXml.ROOT_ELEMENT_NAME:
                    throw new AssertionError();
            }
        });
    }
}
