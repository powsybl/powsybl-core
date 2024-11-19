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
 * A {@link NamingStrategy} implementation that ensures the conformity of IDs with the UCTE-DEF format
 *
 * @author Clément LECLERC {@literal <clement.leclerc@rte-france.com>}
 */
@AutoService(NamingStrategy.class)
public class CounterNamingStrategy implements NamingStrategy {

    private final Map<String, UcteNodeCode> ucteNodeIds = new HashMap<>();
    private final Map<String, UcteElementId> ucteElementIds = new HashMap<>();
    private int namingCounter;
    private static final List<Character> ORDER_CODES = Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '-', '.', ' ');

    @Override
    public String getName() {
        return "Counter";
    }

    /**
     * Converts all network elements to UCTE format using a sequential naming scheme.
     * The conversion process follows these steps:
     * <ol>
     *   <li>Reset the global naming counter to ensure unique identifiers</li>
     *   <li>Convert all elements within each voltage level
     *   <li>Convert network-level elements
     * </ol>
     *
     * Each element receives a unique sequential identifier during the conversion process.
     *
     * @param network the network to convert
     */
    @Override
    public void initialiseNetwork(Network network) {
        namingCounter = 0;
        network.getSubstations().forEach(substation -> substation.getVoltageLevels().forEach(voltageLevel -> {

            voltageLevel.getBusBreakerView().getBuses().forEach(bus -> {
                generateUcteNodeId(bus.getId(), voltageLevel);
            });
            voltageLevel.getBusBreakerView().getSwitches().forEach(sw -> {
                generateUcteElementId(sw);
            });
        }));

        network.getBranchStream().forEach(branch -> {
            generateUcteElementId(branch);
        });

        network.getDanglingLineStream().forEach(d -> {
            generateUcteElementId(d);
        });
    }

    /**
     * Generates a UCTE node code for a network element.
     * The generation process involves:
     * <ol>
     *   <li>Checking for existing node ID to avoid duplicates</li>
     *   <li>Extracting country code from the voltage level</li>
     *   <li>Determining voltage level code based on nominal voltage</li>
     *   <li>Creating a new node ID using namingCounter</li>
     * </ol>
     *
     * The generated UCTE node code follows the format: [CountryCode][NodeId][VoltageLevelCode]
     *
     * @param nodeId original identifier of the network element
     * @param voltageLevel voltage level containing the element
     * @return generated UcteNodeCode, or null if nodeId already exists in ucteNodeIds
     */
    private UcteNodeCode generateUcteNodeId(String nodeId, VoltageLevel voltageLevel) {

        if (UcteNodeCode.isUcteNodeId(nodeId)) {
            Optional<UcteNodeCode> ucteNodeCode = UcteNodeCode.parseUcteNodeCode(nodeId);
            if (ucteNodeCode.isPresent()) {
                ucteNodeIds.put(nodeId, ucteNodeCode.get());
                return ucteNodeCode.get();
            } else {
                throw new UcteException("Invalid ucte node code: " + nodeId);
            }
        }
        if (ucteNodeIds.containsKey(nodeId)) {
            return ucteNodeIds.get(nodeId);
        }

        StringBuilder newNodeCode = new StringBuilder(8);
        String newNodeId = generateIDFromCounter();
        char countryCode = getCountryCode(voltageLevel).getUcteCode();
        char voltageLevelCode = getUcteVoltageLevelCode(voltageLevel.getNominalV());
        char orderCode = ORDER_CODES.get(0);
        int i = 0;

        newNodeCode
                .append(countryCode)
                .append(newNodeId)
                .append(voltageLevelCode)
                .append(orderCode);

        while (ucteNodeIds.containsValue(newNodeCode.toString())) {
            i++;
            orderCode = ORDER_CODES.get(i);
            newNodeCode.replace(7, 7, String.valueOf(orderCode));
        }

        Optional<UcteNodeCode> nodeCode = UcteNodeCode.parseUcteNodeCode(newNodeCode.toString());
        if (!nodeCode.isPresent()) {
            throw new IllegalArgumentException("Invalid node code: " + newNodeCode);
        }
        ucteNodeIds.put(nodeId, nodeCode.get());
        return nodeCode.get();
    }

    private UcteElementId generateUcteElementId(String id, UcteNodeCode node1, UcteNodeCode node2) {

        if (ucteElementIds.containsKey(id)) {
            return ucteElementIds.get(id);
        }
        int i = 0;

        char orderCode = ORDER_CODES.get(0);
        UcteElementId ucteElementId = new UcteElementId(node1, node2, orderCode);

        while (ucteElementIds.containsValue(ucteElementId)) {
            i++;
            if (i < ORDER_CODES.size()) {
                orderCode = ORDER_CODES.get(i);
            }
            ucteElementId = new UcteElementId(node1, node2, orderCode);
        }

        ucteElementIds.put(id, ucteElementId);
        return ucteElementId;
    }

    private UcteElementId generateUcteElementId(Branch<?> branch) {
        if (ucteElementIds.containsKey(branch.getId())) {
            return ucteElementIds.get(branch.getId());
        }
        UcteNodeCode node1 = this.ucteNodeIds.get(branch.getTerminal1().getBusBreakerView().getBus().getId());
        UcteNodeCode node2 = this.ucteNodeIds.get(branch.getTerminal2().getBusBreakerView().getBus().getId());

        return generateUcteElementId(branch.getId(), node1, node2);
    }

    private UcteElementId generateUcteElementId(DanglingLine danglingLine) {
        if (ucteElementIds.containsKey(danglingLine.getId())) {
            return ucteElementIds.get(danglingLine.getId());
        }

        generateUcteNodeId(danglingLine.getPairingKey(), danglingLine.getTerminal().getVoltageLevel());
        generateUcteNodeId(danglingLine.getTerminal().getBusBreakerView().getBus().getId(), danglingLine.getTerminal().getVoltageLevel());
        UcteNodeCode node1 = this.ucteNodeIds.get(danglingLine.getPairingKey());
        UcteNodeCode node2 = this.ucteNodeIds.get(danglingLine.getTerminal().getBusBreakerView().getBus().getId());

        return generateUcteElementId(danglingLine.getId(), node1, node2);
    }

    private UcteElementId generateUcteElementId(Switch sw) {

        if (ucteElementIds.containsKey(sw.getId())) {
            return ucteElementIds.get(sw.getId());
        }

        Bus bus1 = sw.getVoltageLevel().getBusBreakerView().getBus1(sw.getId());
        Bus bus2 = sw.getVoltageLevel().getBusBreakerView().getBus2(sw.getId());
        UcteNodeCode u1 = generateUcteNodeId(bus1.getId(), bus1.getVoltageLevel());
        UcteNodeCode u2 = generateUcteNodeId(bus2.getId(), bus2.getVoltageLevel());

        return generateUcteElementId(sw.getId(), u1, u2);
    }

    private UcteCountryCode getCountryCode(VoltageLevel voltageLevel) {

        UcteCountryCode ucteCountryCode;
        Country country = voltageLevel.getSubstation().get().getCountry().orElseThrow(() -> new com.powsybl.ucte.network.UcteException("No country for this substation"));
        try {
            ucteCountryCode = UcteCountryCode.valueOf(country.name());
        } catch (IllegalArgumentException iae) {
            throw new UcteException("No UCTE country code for " + country.getName());
        }

        return ucteCountryCode;

    }

    private String generateIDFromCounter() {
        namingCounter++;
        if (namingCounter > 99999) {
            int baseNumber = (namingCounter - 100000) % 10000;
            char letter = (char) ('A' + ((namingCounter - 100000) / 10000));
            return String.format("%04d%c", baseNumber, letter);
        }
        return String.format("%05d", namingCounter);
    }

    /**
     * Determines the UCTE voltage level code for a given voltage value.
     * The code is determined by finding the closest standard UCTE voltage level.
     *
     * <p>The method follows these rules:
     * <ul>
     *   <li>For voltage < 27kV: returns '7'</li>
     *   <li>For voltage > 750kV: returns '0'</li>
     *   <li>For other voltages: returns code of the closest standard UCTE level</li>
     * </ul>
     *
     * @param voltage the nominal voltage value in kV
     * @return a character representing the UCTE voltage level code
     */
    private static char getUcteVoltageLevelCode(double voltage) {

        if (voltage < UcteVoltageLevelCode.VL_27.getVoltageLevel()) {
            return '7';
        }
        if (voltage > UcteVoltageLevelCode.VL_750.getVoltageLevel()) {
            return '0';
        }

        UcteVoltageLevelCode closest = null;
        double minDiff = Double.MAX_VALUE;

        for (UcteVoltageLevelCode code : UcteVoltageLevelCode.values()) {
            double diff = Math.abs(voltage - code.getVoltageLevel());
            if (diff < minDiff) {
                minDiff = diff;
                closest = code;
            }
        }
        return closest != null ? (char) ('0' + closest.ordinal()) : '7';
    }

    @Override
    public UcteNodeCode getUcteNodeCode(String id) {
        if (id == null) {
            throw new PowsyblException("ID is null" + id);
        }

        UcteNodeCode code = ucteNodeIds.get(id);
        if (code == null) {
            throw new UcteException("No UCTE code found for id: " + id);
        }
        return code;
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
        UcteElementId elementId = ucteElementIds.get(id);
        if (elementId == null) {
            throw new UcteException("No UCTE element id found for: " + id);
        }
        return elementId;
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
