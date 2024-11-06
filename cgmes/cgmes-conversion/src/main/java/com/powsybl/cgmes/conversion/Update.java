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
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
                || p.propertyNames().contains("AsynchronousMachine");
    }

    static void updateLoads(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.LOAD.name()));

        Map<String, PropertyBag> identifiablePropertyBag = new HashMap<>();
        addPropertyBags(network, cgmes.energyConsumers(), CgmesNames.ENERGY_CONSUMER, identifiablePropertyBag);
        addPropertyBags(network, cgmes.energySources(), CgmesNames.ENERGY_SOURCE, identifiablePropertyBag);
        addPropertyBags(network, cgmes.asynchronousMachines(), CgmesNames.ASYNCHRONOUS_MACHINE, identifiablePropertyBag);

        network.getLoads().forEach(load -> updateLoad(network, load, getPropertyBag(load.getId(), identifiablePropertyBag), context));
        context.popReportNode();
    }

    private static void updateLoad(Network network, Load load, PropertyBag propertyBag, Context context) {
        if (!load.isFictitious()) { // Loads from SvInjections are fictitious
            String originalClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
            PropertyBag cgmesTerminal = getPropertyBagOfCgmesTerminal(load, context);

            switch (originalClass) {
                case CgmesNames.ENERGY_SOURCE -> new EnergySourceConversion(propertyBag, cgmesTerminal, load, context).update(network);
                case CgmesNames.ASYNCHRONOUS_MACHINE ->
                        new AsynchronousMachineConversion(propertyBag, cgmesTerminal, load, context).update(network);
                case CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY, CgmesNames.ENERGY_CONSUMER ->
                        new EnergyConsumerConversion(propertyBag, cgmesTerminal, load, context).update(network);
                default ->
                        throw new ConversionException("Unexpected originalClass " + originalClass + " for Load: " + load.getId());
            }
        }
    }

    private static void addPropertyBags(Network network, PropertyBags propertyBags, String idTag, Map<String, PropertyBag> identifiablePropertyBag) {
        propertyBags.forEach(propertyBag -> {
            String propertyBagId = propertyBag.getId(idTag);
            Identifiable<?> identifiable = network.getIdentifiable(propertyBagId);
            if (identifiable != null) {
                identifiablePropertyBag.put(identifiable.getId(), propertyBag);
            }
        });
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

    private static PropertyBag getPropertyBagOfCgmesTerminal(String cgmesTerminalId, Context context) {
        return context.cgmesTerminal(cgmesTerminalId) != null ? context.cgmesTerminal(cgmesTerminalId) : emptyPropertyBag();
    }

    private static PropertyBag emptyPropertyBag() {
        return new PropertyBag(Collections.emptyList(), false);
    }
}
