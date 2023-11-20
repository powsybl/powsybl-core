/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SubstationXml extends AbstractSimpleIdentifiableXml<Substation, SubstationAdder, Network> {

    static final SubstationXml INSTANCE = new SubstationXml();

    static final String ROOT_ELEMENT_NAME = "substation";
    static final String ARRAY_ELEMENT_NAME = "substations";

    private static final String COUNTRY = "country";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Substation s, Network n, NetworkXmlWriterContext context) {
        Optional<Country> country = s.getCountry();
        country.ifPresent(value -> context.getWriter().writeStringAttribute(COUNTRY, context.getAnonymizer().anonymizeCountry(value).toString()));
        if (s.getTso() != null) {
            context.getWriter().writeStringAttribute("tso", context.getAnonymizer().anonymizeString(s.getTso()));
        }
        if (!s.getGeographicalTags().isEmpty()) {
            context.getWriter().writeStringAttribute("geographicalTags", s.getGeographicalTags().stream()
                    .map(tag -> context.getAnonymizer().anonymizeString(tag))
                    .collect(Collectors.joining(",")));
        }
    }

    @Override
    protected void writeSubElements(Substation s, Network n, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(VoltageLevelXml.ARRAY_ELEMENT_NAME);
        for (VoltageLevel vl : IidmXmlUtil.sorted(s.getVoltageLevels(), context.getOptions())) {
            VoltageLevelXml.INSTANCE.write(vl, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes(TwoWindingsTransformerXml.ARRAY_ELEMENT_NAME);
        Iterable<TwoWindingsTransformer> twts = IidmXmlUtil.sorted(s.getTwoWindingsTransformers(), context.getOptions());
        for (TwoWindingsTransformer twt : twts) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            TwoWindingsTransformerXml.INSTANCE.write(twt, null, context);
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeStartNodes(ThreeWindingsTransformerXml.ARRAY_ELEMENT_NAME);
        Iterable<ThreeWindingsTransformer> twts2 = IidmXmlUtil.sorted(s.getThreeWindingsTransformers(), context.getOptions());
        for (ThreeWindingsTransformer twt : twts2) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            ThreeWindingsTransformerXml.INSTANCE.write(twt, null, context);
        }
        context.getWriter().writeEndNodes();
    }

    @Override
    protected SubstationAdder createAdder(Network network) {
        return network.newSubstation();
    }

    @Override
    protected Substation readRootElementAttributes(SubstationAdder adder, Network network, NetworkXmlReaderContext context) {

        Country country = Optional.ofNullable(context.getReader().readStringAttribute(COUNTRY))
                .map(c -> context.getAnonymizer().deanonymizeCountry(Country.valueOf(c)))
                .orElse(null);
        String tso = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("tso"));
        String geographicalTags = context.getReader().readStringAttribute("geographicalTags");
        if (geographicalTags != null) {
            adder.setGeographicalTags(Arrays.stream(geographicalTags.split(","))
                    .map(tag -> context.getAnonymizer().deanonymizeString(tag))
                    .toArray(String[]::new));
        }
        return adder.setCountry(country)
                .setTso(tso)
                .add();
    }

    @Override
    protected void readSubElements(Substation s, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case VoltageLevelXml.ROOT_ELEMENT_NAME -> VoltageLevelXml.INSTANCE.read(s, context);
                case TwoWindingsTransformerXml.ROOT_ELEMENT_NAME -> TwoWindingsTransformerXml.INSTANCE.read(s, context);
                case ThreeWindingsTransformerXml.ROOT_ELEMENT_NAME -> ThreeWindingsTransformerXml.INSTANCE.read(s, context);
                default -> readSubElement(elementName, s, context);
            }
        });
    }
}
