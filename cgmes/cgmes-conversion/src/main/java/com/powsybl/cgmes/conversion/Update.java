/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.AsynchronousMachineConversion;
import com.powsybl.cgmes.conversion.elements.EnergyConsumerConversion;
import com.powsybl.cgmes.conversion.elements.EnergySourceConversion;
import com.powsybl.cgmes.conversion.elements.transformers.ThreeWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.elements.transformers.TwoWindingsTransformerConversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public final class Update {

    private Update() {
    }

    public static boolean isConvertSeparatedFromUpdate(PropertyBag p) {
        return p.propertyNames().contains("EnergyConsumer")
                || p.propertyNames().contains("ConformLoad")
                || p.propertyNames().contains("NonConformLoad")
                || p.propertyNames().contains("ConformLoad")
                || p.propertyNames().contains("StationSupply")
                || p.propertyNames().contains("TwoWindingsTransformer")
                || p.propertyNames().contains("ThreeWindingsTransformer");
    }

    static void updateLoads(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.LOAD.name()));

        Map<String, PropertyBag> identifiablePropertyBag = new HashMap<>();
        addPropertyBags(cgmes.energyConsumers(), CgmesNames.ENERGY_CONSUMER, identifiablePropertyBag);
        addPropertyBags(cgmes.energySources(), CgmesNames.ENERGY_SOURCE, identifiablePropertyBag);
        addPropertyBags(cgmes.asynchronousMachines(), CgmesNames.ASYNCHRONOUS_MACHINE, identifiablePropertyBag);

        network.getLoads().forEach(load -> updateLoad(load, getPropertyBag(load.getId(), identifiablePropertyBag), context));
        context.popReportNode();
    }

    private static void updateLoad(Load load, PropertyBag cgmesData, Context context) {
        if (!load.isFictitious()) { // Loads from SvInjections are fictitious
            String originalClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
            PropertyBag cgmesTerminal = getPropertyBagOfCgmesTerminal(load, context);

            switch (originalClass) {
                case CgmesNames.ENERGY_SOURCE -> new EnergySourceConversion(cgmesData, cgmesTerminal, load, context).update();
                case CgmesNames.ASYNCHRONOUS_MACHINE ->
                        new AsynchronousMachineConversion(cgmesData, cgmesTerminal, load, context).update();
                case CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY, CgmesNames.ENERGY_CONSUMER ->
                        new EnergyConsumerConversion(cgmesData, cgmesTerminal, load, context).update();
                default ->
                        throw new ConversionException("Unexpected originalClass " + originalClass + " for Load: " + load.getId());
            }
        }
    }

    static void updateTwoAndThreeWindingsTransformers(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.TWO_WINDINGS_TRANSFORMER.name()));
        network.getTwoWindingsTransformers().forEach(t2w -> updateTwoWindingsTransformer(t2w, namedPropertyBag("TwoWindingsTransformer", t2w.getId()), context));
        context.popReportNode();

        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.THREE_WINDINGS_TRANSFORMER.name()));
        network.getThreeWindingsTransformers().forEach(t3w -> updateThreeWindingsTransformer(t3w, namedPropertyBag("ThreeWindingsTransformer", t3w.getId()), context));
        context.popReportNode();
    }

    private static void updateTwoWindingsTransformer(TwoWindingsTransformer t2w, PropertyBag cgmesData, Context context) {
        PropertyBags cgmesTerminals = getPropertyBagsOfCgmesTerminals(t2w, context);
        new TwoWindingsTransformerConversion(cgmesData, cgmesTerminals, t2w, context).update();
    }

    private static void updateThreeWindingsTransformer(ThreeWindingsTransformer t3w, PropertyBag cgmesData, Context context) {
        PropertyBags cgmesTerminals = getPropertyBagsOfCgmesTerminals(t3w, context);
        new ThreeWindingsTransformerConversion(cgmesData, cgmesTerminals, t3w, context).update();
    }

    private static void addPropertyBags(PropertyBags propertyBags, String idTag, Map<String, PropertyBag> identifiablePropertyBag) {
        propertyBags.forEach(propertyBag -> identifiablePropertyBag.put(propertyBag.getId(idTag), propertyBag));
    }

    private static PropertyBag getPropertyBag(String identifiableId, Map<String, PropertyBag> identifiablePropertyBag) {
        return identifiablePropertyBag.containsKey(identifiableId) ? identifiablePropertyBag.get(identifiableId) : emptyPropertyBag();
    }

    private static PropertyBag getPropertyBagOfCgmesTerminal(Connectable<?> connectable, Context context) {
        Optional<String> cgmesTerminalId = connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
        if (cgmesTerminalId.isPresent()) {
            return getPropertyBagOfCgmesTerminal(cgmesTerminalId.get(), context);
        } else {
            return emptyPropertyBag();
        }
    }

    private static PropertyBags getPropertyBagsOfCgmesTerminals(Connectable<?> connectable, Context context) {
        PropertyBags propertyBags = new PropertyBags();
        connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1)
                .ifPresent(cgmesTerminalId -> propertyBags.add(getPropertyBagOfCgmesTerminal(cgmesTerminalId, context)));
        connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2)
                .ifPresent(cgmesTerminalId -> propertyBags.add(getPropertyBagOfCgmesTerminal(cgmesTerminalId, context)));
        connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL3)
                .ifPresent(cgmesTerminalId -> propertyBags.add(getPropertyBagOfCgmesTerminal(cgmesTerminalId, context)));
        return propertyBags;
    }

    private static PropertyBag getPropertyBagOfCgmesTerminal(String cgmesTerminalId, Context context) {
        return context.cgmesTerminal(cgmesTerminalId) != null ? context.cgmesTerminal(cgmesTerminalId) : emptyPropertyBag();
    }

    private static PropertyBag emptyPropertyBag() {
        return new PropertyBag(Collections.emptyList(), false);
    }

    private static PropertyBag namedPropertyBag(String propertyName, String id) {
        PropertyBag propertyBag = new PropertyBag(Collections.emptyList(), false);
        propertyBag.put(propertyName, id);
        return propertyBag;
    }
}
