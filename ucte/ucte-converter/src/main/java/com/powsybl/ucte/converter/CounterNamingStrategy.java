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
import com.powsybl.ucte.converter.util.UcteConstants;
import com.powsybl.ucte.converter.util.UcteConverterHelper;
import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteVoltageLevelCode;

import java.util.*;

/**
 * @author Clément LECLERC {@literal <clement.leclerc@rte-france.com>}
 */
@AutoService(NamingStrategy.class)
public class CounterNamingStrategy extends AbstractNamingStrategy {

    private int voltageLevelCounter;

    @Override
    public String getName() {
        return "Counter";
    }

    @Override
    public void initialiseNetwork(Network network) {
        voltageLevelCounter = 0;
        network.getVoltageLevelStream()
                .forEach(this::processVoltageLevel);

        network.getBranchStream().forEach(this::generateUcteElementId);
        network.getDanglingLineStream().forEach(this::generateUcteElementId);
    }

    private void processVoltageLevel(VoltageLevel voltageLevel) {
        Iterator<Bus> buslist = voltageLevel.getBusBreakerView().getBuses().iterator();
        for (int i = 0; buslist.hasNext(); i++) {
            Bus bus = buslist.next();
            char orderCode = UcteConverterHelper.getOrderCode(i);
            generateUcteNodeId(bus.getId(), voltageLevel, orderCode);
        }

        voltageLevel.getBusBreakerView().getSwitches()
                .forEach(this::generateUcteElementId);
        voltageLevelCounter++;
    }

    private UcteNodeCode generateUcteNodeId(String busId, VoltageLevel voltageLevel, char orderCode) {
        if (UcteNodeCode.isUcteNodeId(busId)) {
            return changeOrderCode(busId, orderCode);
        }
        return createNewUcteNodeId(busId, voltageLevel, orderCode);
    }

    private UcteNodeCode changeOrderCode(String busId, char orderCode) {
        UcteNodeCode newNodeCode = UcteNodeCode.parseUcteNodeCode(busId).orElseThrow();
        newNodeCode.setBusbar(orderCode);
        ucteNodeIds.put(busId, newNodeCode);
        return newNodeCode;
    }

    private UcteNodeCode createNewUcteNodeId(String busId, VoltageLevel voltageLevel, char orderCode) {
        String newNodeId = String.format("%05d", voltageLevelCounter);
        char countryCode = UcteCountryCode.fromVoltagelevel(voltageLevel).getUcteCode();
        char voltageLevelCode = UcteVoltageLevelCode.voltageLevelCodeFromVoltage(voltageLevel.getNominalV());

        UcteNodeCode ucteNodeCode = new UcteNodeCode(
                UcteCountryCode.fromUcteCode(countryCode),
                newNodeId,
                UcteVoltageLevelCode.voltageLevelCodeFromChar(voltageLevelCode),
                orderCode);

        ucteNodeIds.put(busId, ucteNodeCode);
        return ucteNodeCode;
    }

    private UcteElementId generateUcteElementId(String id, UcteNodeCode node1, UcteNodeCode node2) {
        if (ucteElementIds.containsKey(id)) {
            return ucteElementIds.get(id);
        }

        UcteElementId uniqueElementId = UcteConstants.ORDER_CODES.stream()
                .map(orderCode -> new UcteElementId(node1, node2, orderCode))
                .filter(elementId -> !ucteElementIds.containsValue(elementId))
                .findFirst()
                .orElseThrow(() -> new UcteException("Unable to generate unique element ID"));

        ucteElementIds.put(id, uniqueElementId);
        return uniqueElementId;
    }

    private UcteElementId generateUcteElementId(Branch<?> branch) {
        if (ucteElementIds.containsKey(branch.getId())) {
            return ucteElementIds.get(branch.getId());
        }
        UcteNodeCode node1 = ucteNodeIds.get(branch.getTerminal1().getBusBreakerView().getBus().getId());
        UcteNodeCode node2 = ucteNodeIds.get(branch.getTerminal2().getBusBreakerView().getBus().getId());

        return generateUcteElementId(branch.getId(), node1, node2);
    }

    private UcteElementId generateUcteElementId(DanglingLine danglingLine) {
        if (ucteElementIds.containsKey(danglingLine.getId())) {
            return ucteElementIds.get(danglingLine.getId());
        }

        UcteNodeCode code1;
        UcteNodeCode code2;

        code1 = getUcteNodeCode(danglingLine.getTerminal().getBusBreakerView().getBus());

        if (danglingLine.getPairingKey() != null && UcteNodeCode.isUcteNodeId(danglingLine.getPairingKey())) {
            code2 = UcteNodeCode.parseUcteNodeCode(danglingLine.getPairingKey()).orElseThrow();
            ucteNodeIds.put(danglingLine.getPairingKey(), code2);
        } else {
            code2 = generateUcteNodeId(danglingLine.getId(), danglingLine.getTerminal().getVoltageLevel(), UcteConverterHelper.getOrderCode(0));
        }
        return generateUcteElementId(danglingLine.getId(), code1, code2);
    }

    private UcteElementId generateUcteElementId(Switch sw) {
        if (ucteElementIds.containsKey(sw.getId())) {
            return ucteElementIds.get(sw.getId());
        }

        VoltageLevel.BusBreakerView view = sw.getVoltageLevel().getBusBreakerView();
        Bus bus1 = view.getBus1(sw.getId());
        Bus bus2 = view.getBus2(sw.getId());

        UcteNodeCode u1 = getUcteNodeCode(bus1.getId());
        UcteNodeCode u2 = getUcteNodeCode(bus2.getId());

        return generateUcteElementId(sw.getId(), u1, u2);
    }

}
