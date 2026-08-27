/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteVoltageLevelCode;
import com.powsybl.ucte.network.util.UcteNetworkUtil;

import java.util.*;

/**
 * @author Clément LECLERC {@literal <clement.leclerc@rte-france.com>}
 */
@AutoService(NamingStrategy.class)
public class CounterNamingStrategy extends AbstractNamingStrategy {

    @Override
    public String getName() {
        return "Counter";
    }

    @Override
    public Context initialize(Network network) {
        Context context = new Context(network);

        network.getVoltageLevelStream()
                .forEach(voltageLevel -> processVoltageLevel(context, voltageLevel));

        network.getBranchStream().forEach(branch -> generateUcteElementId(context, branch));
        network.getBoundaryLineStream().forEach(boundaryLine -> generateUcteElementId(context, boundaryLine));

        return context;
    }

    private void processVoltageLevel(Context context, VoltageLevel voltageLevel) {
        Iterator<Bus> buslist = voltageLevel.getBusBreakerView().getBuses().iterator();
        for (int i = 0; buslist.hasNext(); i++) {
            Bus bus = buslist.next();
            char orderCode = UcteNetworkUtil.getOrderCode(i);
            generateUcteNodeId(context, bus.getId(), voltageLevel, orderCode);
        }

        voltageLevel.getBusBreakerView().getSwitches()
                .forEach(sw -> generateUcteElementId(context, sw));
        context.voltageLevelCounter++;
    }

    private UcteNodeCode generateUcteNodeId(Context context, String busId, VoltageLevel voltageLevel, char orderCode) {
        if (UcteNodeCode.isUcteNodeId(busId)) {
            return changeOrderCode(context, busId, orderCode);
        }
        return createNewUcteNodeId(context, busId, voltageLevel, orderCode);
    }

    private UcteNodeCode changeOrderCode(Context context, String busId, char orderCode) {
        UcteNodeCode newNodeCode = UcteNodeCode.parseUcteNodeCode(busId).orElseThrow();
        newNodeCode.setBusbar(orderCode);
        context.getUcteNodeIds().put(busId, newNodeCode);
        return newNodeCode;
    }

    private UcteNodeCode createNewUcteNodeId(Context context, String busId, VoltageLevel voltageLevel, char orderCode) {
        String newNodeId = String.format("%05d", context.voltageLevelCounter);
        char countryCode = UcteCountryCode.fromVoltagelevel(voltageLevel).getUcteCode();
        char voltageLevelCode = UcteVoltageLevelCode.voltageLevelCodeFromVoltage(voltageLevel.getNominalV());

        UcteNodeCode ucteNodeCode = new UcteNodeCode(
                UcteCountryCode.fromUcteCode(countryCode),
                newNodeId,
                UcteVoltageLevelCode.voltageLevelCodeFromChar(voltageLevelCode),
                orderCode);

        context.getUcteNodeIds().put(busId, ucteNodeCode);
        return ucteNodeCode;
    }

    private UcteElementId generateUcteElementId(Context context, String id, UcteNodeCode node1, UcteNodeCode node2) {
        Map<String, UcteElementId> ucteElementIds = context.getUcteElementIds();
        if (ucteElementIds.containsKey(id)) {
            return ucteElementIds.get(id);
        }

        UcteElementId uniqueElementId = UcteNetworkUtil.ORDER_CODES.stream()
                .map(orderCode -> new UcteElementId(node1, node2, orderCode))
                .filter(elementId -> !ucteElementIds.containsValue(elementId))
                .findFirst()
                .orElseThrow(() -> new UcteException("Unable to generate unique element ID"));

        ucteElementIds.put(id, uniqueElementId);
        return uniqueElementId;
    }

    private UcteElementId generateUcteElementId(Context context, Branch<?> branch) {
        Map<String, UcteElementId> ucteElementIds = context.getUcteElementIds();
        if (ucteElementIds.containsKey(branch.getId())) {
            return ucteElementIds.get(branch.getId());
        }
        Map<String, UcteNodeCode> ucteNodeIds = context.getUcteNodeIds();
        UcteNodeCode node1 = ucteNodeIds.get(branch.getTerminal1().getBusBreakerView().getBus().getId());
        UcteNodeCode node2 = ucteNodeIds.get(branch.getTerminal2().getBusBreakerView().getBus().getId());

        return generateUcteElementId(context, branch.getId(), node1, node2);
    }

    private UcteElementId generateUcteElementId(Context context, BoundaryLine boundaryLine) {
        if (context.getUcteElementIds().containsKey(boundaryLine.getId())) {
            return context.getUcteElementIds().get(boundaryLine.getId());
        }

        UcteNodeCode code1;
        UcteNodeCode code2;

        code1 = getUcteNodeCode(context, boundaryLine.getTerminal().getBusBreakerView().getBus());

        if (boundaryLine.getPairingKey() != null && UcteNodeCode.isUcteNodeId(boundaryLine.getPairingKey())) {
            code2 = UcteNodeCode.parseUcteNodeCode(boundaryLine.getPairingKey()).orElseThrow();
            context.getUcteNodeIds().put(boundaryLine.getPairingKey(), code2);
        } else {
            code2 = generateUcteNodeId(context, boundaryLine.getId(), boundaryLine.getTerminal().getVoltageLevel(), UcteNetworkUtil.getOrderCode(0));
        }
        return generateUcteElementId(context, boundaryLine.getId(), code1, code2);
    }

    private UcteElementId generateUcteElementId(Context context, Switch sw) {
        Map<String, UcteElementId> ucteElementIds = context.getUcteElementIds();
        if (ucteElementIds.containsKey(sw.getId())) {
            return ucteElementIds.get(sw.getId());
        }

        VoltageLevel.BusBreakerView view = sw.getVoltageLevel().getBusBreakerView();
        Bus bus1 = view.getBus1(sw.getId());
        Bus bus2 = view.getBus2(sw.getId());

        UcteNodeCode u1 = getUcteNodeCode(context, bus1.getId());
        UcteNodeCode u2 = getUcteNodeCode(context, bus2.getId());

        return generateUcteElementId(context, sw.getId(), u1, u2);
    }

    /**
     * Adds the running voltage-level counter used to synthesize new node ids on top of the base
     * {@link AbstractNamingStrategy.Context}.
     */
    public static class Context extends AbstractNamingStrategy.Context {

        private int voltageLevelCounter;

        public Context(Network network) {
            super(network);
        }
    }

}
