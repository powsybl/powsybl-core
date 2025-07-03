/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TieLineUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion.identify;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class TieLineConversion {

    private TieLineConversion() {
    }

    public static void create(String node, EquipmentAtBoundaryConversion conversion1, EquipmentAtBoundaryConversion conversion2, Context context) {
        conversion1.convertAtBoundary();
        Optional<DanglingLine> dl1 = conversion1.getDanglingLine();
        conversion2.convertAtBoundary();
        Optional<DanglingLine> dl2 = conversion2.getDanglingLine();

        if (dl1.isPresent() && dl2.isPresent()) {
            // there can be several dangling lines linked to same x-node in one IGM for planning purposes
            // in this case, we don't merge them
            // please note that only one of them should be connected
            String regionName1 = obtainRegionName(dl1.get().getTerminal().getVoltageLevel());
            String regionName2 = obtainRegionName(dl2.get().getTerminal().getVoltageLevel());

            String pairingKey1 = dl1.get().getPairingKey();
            String pairingKey2 = dl2.get().getPairingKey();

            if (!(pairingKey1 != null && pairingKey1.equals(pairingKey2))) {
                context.ignored(node, "Both dangling lines do not have the same pairingKey: we do not consider them as a merged line");
            } else if (regionName1 != null && regionName1.equals(regionName2)) {
                context.ignored(node, "Both dangling lines are in the same region: we do not consider them as a merged line");
            } else if (dl2.get().getId().compareTo(dl1.get().getId()) >= 0) {
                convertToTieLine(context, dl1.get(), dl2.get());
            } else {
                convertToTieLine(context, dl2.get(), dl1.get());
            }
        }
    }

    private static String obtainRegionName(VoltageLevel voltageLevel) {
        return voltageLevel.getSubstation().map(s -> s.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionName")).orElse(null);
    }

    private static void convertToTieLine(Context context, DanglingLine dl1, DanglingLine dl2) {
        TieLineAdder adder = context.network().newTieLine()
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId());
        identify(context, adder, context.namingStrategy().getIidmId("TieLine", TieLineUtil.buildMergedId(dl1.getId(), dl2.getId())),
                TieLineUtil.buildMergedName(dl1.getId(), dl2.getId(), dl1.getNameOrId(), dl2.getNameOrId()));
        adder.add();
    }

    public static void createDuringUpdate(Network network, Context context) {
        network.getDanglingLineStream()
                .filter(danglingLine -> !danglingLine.isPaired())
                .collect(Collectors.groupingBy(DanglingLine::getPairingKey))
                .values().forEach(danglingLinesList -> {
                    List<DanglingLine> connectedDanglingLines = danglingLinesList.stream()
                            .filter(danglingLine -> isConnected(danglingLine, context))
                            .sorted(Comparator.comparing(Identifiable::getId)).toList();

                    if (connectedDanglingLines.size() == 2 && !isSameRegion(connectedDanglingLines.get(0), connectedDanglingLines.get(1))) {
                        convertToTieLine(context, connectedDanglingLines.get(0), connectedDanglingLines.get(1));
                    }
                });
    }

    // We use the raw terminal connected attribute received in CGMES because in nodeBreaker models,
    // depending on the configuration, this information is not reflected in the terminal status of the danglingLine
    private static boolean isConnected(DanglingLine danglingLine, Context context) {
        return danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1)
                .map(context::cgmesTerminal)
                .map(cgmesData -> cgmesData.asBoolean(CgmesNames.CONNECTED, true))
                .orElse(true);
    }

    private static boolean isSameRegion(DanglingLine danglingLine1, DanglingLine danglingLine2) {
        String region1 = obtainRegionName(danglingLine1.getTerminal().getVoltageLevel());
        String region2 = obtainRegionName(danglingLine2.getTerminal().getVoltageLevel());
        return region1 != null && region1.equals(region2);
    }
}
