/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
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

    private int namingCounter;

    @Override
    public String getName() {
        return "Counter";
    }

    @Override
    public void initialiseNetwork(Network network) {
        namingCounter = 0;
        network.getVoltageLevelStream()
                .forEach(this::processVoltageLevel);

        network.getBranchStream().forEach(this::generateUcteElementId);
        network.getDanglingLineStream().forEach(this::generateUcteElementId);
    }

    private void processVoltageLevel(VoltageLevel voltageLevel) {
        Iterator<Bus> buslist = voltageLevel.getBusBreakerView().getBuses().iterator();
        for (int i = 0; buslist.hasNext(); i++) {
            Bus bus = buslist.next();
            generateUcteNodeId(bus.getId(), voltageLevel, i);
        }

        voltageLevel.getBusBreakerView().getSwitches()
                .forEach(this::generateUcteElementId);
        namingCounter++;
    }

    private UcteNodeCode generateUcteNodeId(String nodeId, VoltageLevel voltageLevel, int orderCodeIndex) {
        if (UcteNodeCode.isUcteNodeId(nodeId)) {
            return processExistingUcteNodeId(nodeId, orderCodeIndex);
        }
        if (ucteNodeIds.containsKey(nodeId)) {
            return ucteNodeIds.get(nodeId);
        }
        return createNewUcteNodeId(nodeId, voltageLevel, orderCodeIndex);
    }

    private UcteNodeCode processExistingUcteNodeId(String nodeId, int orderCodeIndex) {
        String newNodeId = nodeId.substring(0, nodeId.length() - 1) + orderCodeIndex;
        Optional<UcteNodeCode> nodeCode = UcteNodeCode.parseUcteNodeCode(newNodeId);
        if (nodeCode.isPresent()) {
            ucteNodeIds.put(nodeId, nodeCode.get());
            return nodeCode.get();
        }
        throw new PowsyblException(String.format(NO_UCTE_CODE_ERROR, newNodeId));
    }

    private UcteNodeCode createNewUcteNodeId(String nodeId, VoltageLevel voltageLevel, int orderCodeIndex) {
        String newNodeId = String.format("%05d", namingCounter);
        char countryCode = getCountryCode(voltageLevel).getUcteCode();
        char voltageLevelCode = UcteVoltageLevelCode.voltageLevelCodeFromVoltage(voltageLevel.getNominalV());
        if (orderCodeIndex > UcteElementId.ORDER_CODES.get(orderCodeIndex)) {
            throw new PowsyblException("Order code index out of bounds");
        }
        char orderCode = UcteElementId.ORDER_CODES.get(orderCodeIndex);

        UcteNodeCode ucteNodeCode = new UcteNodeCode(
                UcteCountryCode.fromUcteCode(countryCode),
                newNodeId,
                UcteVoltageLevelCode.voltageLevelCodeFromChar(voltageLevelCode),
                orderCode);

        ucteNodeIds.put(nodeId, ucteNodeCode);
        return ucteNodeCode;
    }

    private UcteCountryCode getCountryCode(VoltageLevel voltageLevel) {
        Country country = voltageLevel.getSubstation()
                .flatMap(Substation::getCountry)
                .orElseThrow(() -> new UcteException(NO_COUNTRY_ERROR));

        try {
            return UcteCountryCode.valueOf(country.name());
        } catch (IllegalArgumentException e) {
            throw new UcteException(String.format(NO_UCTE_COUNTRY_ERROR, country.getName()));
        }
    }

    private UcteElementId generateUcteElementId(String id, UcteNodeCode node1, UcteNodeCode node2) {
        if (ucteElementIds.containsKey(id)) {
            return ucteElementIds.get(id);
        }

        UcteElementId uniqueElementId = UcteElementId.ORDER_CODES.stream()
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

        if (danglingLine.getPairingKey() != null) {
            code2 = generateUcteNodeId(danglingLine.getPairingKey(), danglingLine.getTerminal().getVoltageLevel(), 0);
        } else {
            code2 = generateUcteNodeId(danglingLine.getId(), danglingLine.getTerminal().getVoltageLevel(), 0);
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
