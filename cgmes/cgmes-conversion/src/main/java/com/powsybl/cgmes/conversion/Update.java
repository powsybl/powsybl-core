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

    static void updateLoads(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.LOAD.name()));

        Map<String, PropertyBag> equipmentIdPropertyBag = new HashMap<>();
        addPropertyBags(cgmes.energyConsumers(), CgmesNames.ENERGY_CONSUMER, equipmentIdPropertyBag);
        addPropertyBags(cgmes.energySources(), CgmesNames.ENERGY_SOURCE, equipmentIdPropertyBag);
        addPropertyBags(cgmes.asynchronousMachines(), CgmesNames.ASYNCHRONOUS_MACHINE, equipmentIdPropertyBag);

        network.getLoads().forEach(load -> updateLoad(load, getPropertyBag(load.getId(), equipmentIdPropertyBag), context));
        context.popReportNode();
    }

    private static void updateLoad(Load load, PropertyBag cgmesData, Context context) {
        if (!load.isFictitious()) { // Loads from SvInjections are fictitious
            String originalClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);

            switch (originalClass) {
                case CgmesNames.ENERGY_SOURCE -> EnergySourceConversion.update(load, cgmesData, context);
                case CgmesNames.ASYNCHRONOUS_MACHINE -> AsynchronousMachineConversion.update(load, cgmesData, context);
                case CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY, CgmesNames.ENERGY_CONSUMER ->
                        EnergyConsumerConversion.update(load, cgmesData, context);
                default ->
                        throw new ConversionException("Unexpected originalClass " + originalClass + " for Load: " + load.getId());
            }
        }
    }

    static void updateTransformers(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.TWO_WINDINGS_TRANSFORMER.name()));
        network.getTwoWindingsTransformers().forEach(t2w -> TwoWindingsTransformerConversion.update(t2w, context));
        context.popReportNode();

        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.THREE_WINDINGS_TRANSFORMER.name()));
        network.getThreeWindingsTransformers().forEach(t3w -> ThreeWindingsTransformerConversion.update(t3w, context));
        context.popReportNode();
    }

    private static void addPropertyBags(PropertyBags propertyBags, String idTag, Map<String, PropertyBag> equipmentIdPropertyBag) {
        propertyBags.forEach(propertyBag -> equipmentIdPropertyBag.put(propertyBag.getId(idTag), propertyBag));
    }

    private static PropertyBag getPropertyBag(String identifiableId, Map<String, PropertyBag> equipmentIdPropertyBag) {
        return equipmentIdPropertyBag.containsKey(identifiableId) ? equipmentIdPropertyBag.get(identifiableId) : emptyPropertyBag();
    }

    private static PropertyBag emptyPropertyBag() {
        return new PropertyBag(Collections.emptyList(), false);
    }
}