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
public class CounterNamingStrategy implements NamingStrategy {
    private static final String NO_COUNTRY_ERROR = "No country for this substation";
    private static final String NO_UCTE_CODE_ERROR = "No UCTE code found for id: %s";
    private static final String NO_UCTE_COUNTRY_ERROR = "No UCTE country code for %s";
    private static final String INVALID_NODE_CODE_ERROR = "Invalid ucte node code: %s";

    private final Map<String, UcteNodeCode> ucteNodeIds = new HashMap<>();
    private final Map<String, UcteElementId> ucteElementIds = new HashMap<>();
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
        voltageLevel.getBusBreakerView().getBuses()
                .forEach(bus -> generateUcteNodeId(bus.getId(), voltageLevel));
        voltageLevel.getBusBreakerView().getSwitches()
                .forEach(this::generateUcteElementId);
    }

    private UcteNodeCode generateUcteNodeId(String nodeId, VoltageLevel voltageLevel) {
        if (UcteNodeCode.isUcteNodeId(nodeId)) {
            return processExistingUcteNodeId(nodeId);
        }
        if (ucteNodeIds.containsKey(nodeId)) {
            return ucteNodeIds.get(nodeId);
        }
        return createNewUcteNodeId(nodeId, voltageLevel);
    }

    private UcteNodeCode processExistingUcteNodeId(String nodeId) {
        return UcteNodeCode.parseUcteNodeCode(nodeId)
                .map(code -> {
                    ucteNodeIds.put(nodeId, code);
                    return code;
                })
                .orElseThrow(() -> new UcteException(String.format(INVALID_NODE_CODE_ERROR, nodeId)));
    }

    private UcteNodeCode createNewUcteNodeId(String nodeId, VoltageLevel voltageLevel) {
        String newNodeId = String.format("%05d", namingCounter++);
        char countryCode = getCountryCode(voltageLevel).getUcteCode();
        char voltageLevelCode = UcteVoltageLevelCode.voltageLevelCodeFromVoltage(voltageLevel.getNominalV());

        return generateUniqueNodeCode(nodeId, newNodeId, countryCode, voltageLevelCode);
    }

    private UcteNodeCode generateUniqueNodeCode(String nodeId, String newNodeId,
                                                char countryCode, char voltageLevelCode) {
        for (Character orderCode : UcteElementId.ORDER_CODES) {

            UcteNodeCode ucteNodeCode = new UcteNodeCode(
                    UcteCountryCode.fromUcteCode(countryCode),
                    newNodeId,
                    UcteVoltageLevelCode.voltageLevelCodeFromChar(voltageLevelCode),
                    orderCode);
            if (!ucteNodeIds.containsValue(ucteNodeCode)) {
                ucteNodeIds.put(nodeId, ucteNodeCode);
                return ucteNodeCode;
            }
        }
        throw new UcteException("Unable to generate unique node code");
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

        return UcteElementId.ORDER_CODES.stream()
                .map(orderCode -> new UcteElementId(node1, node2, orderCode))
                .filter(elementId -> !ucteElementIds.containsValue(elementId))
                .findFirst()
                .map(elementId -> {
                    ucteElementIds.put(id, elementId);
                    return elementId;
                })
                .orElseThrow(() -> new UcteException("Unable to generate unique element ID"));
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

        if (danglingLine.getPairingKey() != null) {
            code1 = generateUcteNodeId(danglingLine.getPairingKey(), danglingLine.getTerminal().getVoltageLevel());
        } else {
            code1 = generateUcteNodeId(danglingLine.getId(), danglingLine.getTerminal().getVoltageLevel());
        }
        code2 = generateUcteNodeId(danglingLine.getTerminal().getBusBreakerView().getBus().getId(), danglingLine.getTerminal().getVoltageLevel());
        return generateUcteElementId(danglingLine.getId(), code1, code2);
    }

    private UcteElementId generateUcteElementId(Switch sw) {
        if (ucteElementIds.containsKey(sw.getId())) {
            return ucteElementIds.get(sw.getId());
        }

        VoltageLevel.BusBreakerView view = sw.getVoltageLevel().getBusBreakerView();
        Bus bus1 = view.getBus1(sw.getId());
        Bus bus2 = view.getBus2(sw.getId());

        UcteNodeCode u1 = generateUcteNodeId(bus1.getId(), bus1.getVoltageLevel());
        UcteNodeCode u2 = generateUcteNodeId(bus2.getId(), bus2.getVoltageLevel());

        return generateUcteElementId(sw.getId(), u1, u2);
    }

    @Override
    public UcteNodeCode getUcteNodeCode(String id) {
        if (id == null) {
            throw new PowsyblException("ID is null");
        }
        return Optional.ofNullable(ucteNodeIds.get(id))
                .orElseThrow(() -> new UcteException(String.format(NO_UCTE_CODE_ERROR, id)));
    }

    @Override
    public UcteNodeCode getUcteNodeCode(Bus bus) {
        return getUcteNodeCode(bus.getId());
    }

    @Override
    public UcteNodeCode getUcteNodeCode(DanglingLine danglingLine) {
        return getUcteNodeCode(danglingLine.getPairingKey());
    }

    @Override
    public UcteElementId getUcteElementId(String id) {
        return Optional.ofNullable(ucteElementIds.get(id))
                .orElseThrow(() -> new UcteException(String.format("No UCTE element id found for: %s", id)));
    }

    @Override
    public UcteElementId getUcteElementId(Switch sw) {
        return getUcteElementId(sw.getId());
    }

    @Override
    public UcteElementId getUcteElementId(Branch branch) {
        return getUcteElementId(branch.getId());
    }

    @Override
    public UcteElementId getUcteElementId(DanglingLine danglingLine) {
        return getUcteElementId(danglingLine.getId());
    }
}
