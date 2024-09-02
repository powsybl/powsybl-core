/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SubstationSerDe extends AbstractSimpleIdentifiableSerDe<Substation, SubstationAdder, Network> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubstationSerDe.class);

    static final SubstationSerDe INSTANCE = new SubstationSerDe();

    static final String ROOT_ELEMENT_NAME = "substation";
    static final String ARRAY_ELEMENT_NAME = "substations";

    private static final String COUNTRY = "country";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Substation s, Network n, NetworkSerializerContext context) {
        context.getWriter().writeStringAttribute(COUNTRY, s.getCountry().map(c -> context.getAnonymizer().anonymizeCountry(c).toString()).orElse(null));
        context.getWriter().writeStringAttribute("tso", Optional.ofNullable(s.getTso()).map(tso -> context.getAnonymizer().anonymizeString(tso)).orElse(null));
        context.getWriter().writeStringArrayAttribute("geographicalTags", s.getGeographicalTags().stream().map(tag -> context.getAnonymizer().anonymizeString(tag)).toList());
    }

    @Override
    protected void writeSubElements(Substation s, Network n, NetworkSerializerContext context) {
        writeVoltageLevels(s, context);
        writeTwoWindingsTransformers(s, context);
        writeThreeWindingsTransformers(s, context);
        writeOverloadManagementSystems(s, context);
    }

    private static void writeVoltageLevels(Substation s, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (VoltageLevel vl : IidmSerDeUtil.sorted(s.getVoltageLevels(), context.getOptions())) {
            VoltageLevelSerDe.INSTANCE.write(vl, null, context);
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeTwoWindingsTransformers(Substation s, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        Iterable<TwoWindingsTransformer> twts = IidmSerDeUtil.sorted(s.getTwoWindingsTransformers(), context.getOptions());
        for (TwoWindingsTransformer twt : twts) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            TwoWindingsTransformerSerDe.INSTANCE.write(twt, null, context);
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeThreeWindingsTransformers(Substation s, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        Iterable<ThreeWindingsTransformer> twts = IidmSerDeUtil.sorted(s.getThreeWindingsTransformers(), context.getOptions());
        for (ThreeWindingsTransformer twt : twts) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            ThreeWindingsTransformerSerDe.INSTANCE.write(twt, null, context);
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeOverloadManagementSystems(Substation s, NetworkSerializerContext context) {
        if (!context.getOptions().isWithAutomationSystems()) {
            return;
        }
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            Collection<OverloadManagementSystem> validOverloadManagementSystems = filterValidOverloadManagementSystems(s);
            if (!validOverloadManagementSystems.isEmpty()) {
                context.getWriter().writeStartNodes();
                IidmSerDeUtil.sorted(validOverloadManagementSystems, context.getOptions())
                        .forEach(oms -> OverloadManagementSystemSerDe.INSTANCE.write(oms, null, context));
                context.getWriter().writeEndNodes();
            }
        });
    }

    private static Collection<OverloadManagementSystem> filterValidOverloadManagementSystems(Substation s) {
        Network n = s.getNetwork();
        return s.getOverloadManagementSystemStream().filter(o -> {
            if (n.getIdentifiable(o.getMonitoredElementId()) == null) {
                LOGGER.warn(String.format("Discard overload management system '%s': monitored element '%s' is unknown.",
                        o.getNameOrId(), o.getMonitoredElementId()));
                return false;
            }
            for (OverloadManagementSystem.Tripping tripping : o.getTrippings()) {
                Identifiable<?> element = null;
                String id = "";
                String type = "";
                switch (tripping.getType()) {
                    case BRANCH_TRIPPING -> {
                        type = "branch";
                        id = ((OverloadManagementSystem.BranchTripping) tripping).getBranchToOperateId();
                        element = n.getBranch(id);
                    }
                    case SWITCH_TRIPPING -> {
                        type = "switch";
                        id = ((OverloadManagementSystem.SwitchTripping) tripping).getSwitchToOperateId();
                        element = n.getSwitch(id);
                    }
                    case THREE_WINDINGS_TRANSFORMER_TRIPPING -> {
                        type = "three windings transformer";
                        id = ((OverloadManagementSystem.ThreeWindingsTransformerTripping) tripping).getThreeWindingsTransformerToOperateId();
                        element = n.getThreeWindingsTransformer(id);
                    }
                }
                if (element == null) {
                    LOGGER.warn(String.format("Discard overload management system '%s': invalid %s tripping. '%s' is unknown.",
                            o.getNameOrId(), type, id));
                    return false;
                }
            }
            return true;
        }).toList();
    }

    @Override
    protected SubstationAdder createAdder(Network network) {
        return network.newSubstation();
    }

    @Override
    protected Substation readRootElementAttributes(SubstationAdder adder, Network network, NetworkDeserializerContext context) {

        Country country = Optional.ofNullable(context.getReader().readStringAttribute(COUNTRY))
                .map(c -> context.getAnonymizer().deanonymizeCountry(Country.valueOf(c)))
                .orElse(null);
        String tso = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("tso"));
        String[] geographicalTags = context.getReader().readStringArrayAttribute("geographicalTags").stream()
                .map(tag -> context.getAnonymizer().deanonymizeString(tag)).toArray(String[]::new);
        return adder.setCountry(country)
                .setTso(tso)
                .setGeographicalTags(geographicalTags)
                .add();
    }

    @Override
    protected void readSubElements(Substation s, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case VoltageLevelSerDe.ROOT_ELEMENT_NAME -> VoltageLevelSerDe.INSTANCE.read(s, context);
                case TwoWindingsTransformerSerDe.ROOT_ELEMENT_NAME -> TwoWindingsTransformerSerDe.INSTANCE.read(s, context);
                case ThreeWindingsTransformerSerDe.ROOT_ELEMENT_NAME -> ThreeWindingsTransformerSerDe.INSTANCE.read(s, context);
                case OverloadManagementSystemSerDe.ROOT_ELEMENT_NAME -> checkSupportedAndReadOverloadManagementSystems(s, context);
                default -> readSubElement(elementName, s, context);
            }
        });
    }

    private static void checkSupportedAndReadOverloadManagementSystems(Substation s, NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, OverloadManagementSystemSerDe.ROOT_ELEMENT_NAME,
                IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
        if (context.getOptions().isWithAutomationSystems()) {
            OverloadManagementSystemSerDe.INSTANCE.read(s, context);
        } else {
            OverloadManagementSystemSerDe.INSTANCE.skip(s, context);
        }
    }
}
