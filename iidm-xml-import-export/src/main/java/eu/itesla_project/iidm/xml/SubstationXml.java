/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationXml extends IdentifiableXml<Substation, SubstationAdder, Network> {

    static final SubstationXml INSTANCE = new SubstationXml();

    static final String ROOT_ELEMENT_NAME = "substation";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Substation s) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(Substation s, Network n, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("country", s.getCountry().toString());
        if (s.getTso() != null) {
            context.getWriter().writeAttribute("tso", s.getTso());
        }
        if (s.getGeographicalTags().size() > 0) {
            context.getWriter().writeAttribute("geographicalTags", s.getGeographicalTags().stream().collect(Collectors.joining(",")));
        }
    }

    @Override
    protected void writeSubElements(Substation s, Network n, XmlWriterContext context) throws XMLStreamException {
        for (VoltageLevel vl : s.getVoltageLevels()) {
            VoltageLevelXml.INSTANCE.write(vl, null, context);
        }
        Iterable<TwoWindingsTransformer> twts = s.getTwoWindingsTransformers();
        for (TwoWindingsTransformer twt : twts) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            TwoWindingsTransformerXml.INSTANCE.write(twt, null, context);
        }
        Iterable<ThreeWindingsTransformer> twts2 = s.getThreeWindingsTransformers();
        for (ThreeWindingsTransformer twt : twts2) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            ThreeWindingsTransformerXml.INSTANCE.write(twt, null, context);
        }
    }

    @Override
    protected SubstationAdder createAdder(Network network) {
        return network.newSubstation();
    }

    @Override
    protected Substation readRootElementAttributes(SubstationAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        Country country = Country.valueOf(reader.getAttributeValue(null, "country"));
        String tso = reader.getAttributeValue(null, "tso");
        String geographicalTags = reader.getAttributeValue(null, "geographicalTags");
        if (geographicalTags != null) {
            adder.setGeographicalTags(geographicalTags.split(","));
        }
        return adder.setCountry(country)
                .setTso(tso)
                .add();
    }

    @Override
    protected void readSubElements(Substation s, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            switch (reader.getLocalName()) {
                case VoltageLevelXml.ROOT_ELEMENT_NAME:
                    VoltageLevelXml.INSTANCE.read(reader, s, endTasks);
                    break;

                case TwoWindingsTransformerXml.ROOT_ELEMENT_NAME:
                    TwoWindingsTransformerXml.INSTANCE.read(reader, s, endTasks);
                    break;

                case ThreeWindingsTransformerXml.ROOT_ELEMENT_NAME:
                    ThreeWindingsTransformerXml.INSTANCE.read(reader, s, endTasks);
                    break;

                default:
                    super.readSubElements(s, reader, endTasks);
            }
        });
    }
}
