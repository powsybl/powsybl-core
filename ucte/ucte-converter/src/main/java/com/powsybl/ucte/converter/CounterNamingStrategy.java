/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public String getName() {
        return "CounterNamingStrategy";
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
    public void convertNetworkToUcte(Network network) {
        //count for naming each element
        namingCounter = 0;
        // Convert voltage level elements
        network.getVoltageLevelStream().sorted(Comparator.comparing(VoltageLevel::getId)).forEach(voltageLevel ->
                convertVoltageLevelElementsIdToUcte(voltageLevel));
        // Convert network elements
        convertNetworkElementsIdToUcte(network);
    }


    /**
     * Converts all voltageLevel elements to UCTE format using a sequential naming scheme.
     * The conversion process follows these steps:
     * <ol>
     *   <li>Convert all elements within each voltage level (buses, generators, loads, shunts)</li>
     * </ol>
     *
     * Each element receives a unique sequential identifier during the conversion process.
     *
     * @param voltageLevel the voltageLevel to convert
     */
    private void convertVoltageLevelElementsIdToUcte(VoltageLevel voltageLevel) {
        System.out.println(voltageLevel.getId());
        namingCounter++;
        int nodeCounter=0;

       for(Bus bus : voltageLevel.getBusBreakerView().getBuses()){
           nodeCounter++;
           UcteNodeCode ucteId = generateUcteNodeId(bus.getId(), voltageLevel,nodeCounter);
           if (ucteId != null) {
               bus.setName(ucteId.toString());
               logConversion("BUS", bus);
           }
       }

    }

    /**
     * Converts all network-level elements to UCTE format using a sequential naming scheme.
     * The conversion process follows these steps:
     * <ol>
     *   <li>Convert network-level elements (lines, transformers)</li>
     * </ol>
     *
     * Each element receives a unique sequential identifier during the conversion process.
     *
     * @param network the network to convert
     */
    private void convertNetworkElementsIdToUcte(Network network) {
        network.getBranchStream().forEach(branch -> {
            UcteElementId ucteId = generateUcteElementId(branch);
            branch.setName(ucteId.toString());
            logConversion(branch.getType().name(), branch);
        });
    }

    private void logConversion(String elementType, Identifiable element) {
        System.out.println(String.format("%s : %s--> %s",
                elementType,
                element.getId(),
                element.getNameOrId()));
    }

    /**
     * Generates a UCTE node code for a network element.
     * The generation process involves:
     * <ol>
     *   <li>Checking for existing node ID to avoid duplicates</li>
     *   <li>Extracting country code from the voltage level</li>
     *   <li>Determining voltage level code based on nominal voltage</li>
     *   <li>Creating a new node ID using the first alphanumeric character of voltage level ID and counter</li>
     * </ol>
     *
     * The generated UCTE node code follows the format: [CountryCode][NodeId][VoltageLevelCode]
     *
     * @param nodeId original identifier of the network element
     * @param voltageLevel voltage level containing the element
     * @return generated UcteNodeCode, or null if nodeId already exists in ucteNodeIds
     */
    private UcteNodeCode generateUcteNodeId(String nodeId, VoltageLevel voltageLevel,int nodeCounter) {

        if (ucteNodeIds.containsKey(nodeId)) {
            return null;
        }
        StringBuilder newNodeCode = new StringBuilder(8);
        String newNodeId = String.format("%05d",namingCounter);
        char countryCode = getCountryCode(voltageLevel);
        char voltageLevelCode = getUcteVoltageLevelCode(voltageLevel.getNominalV());

        newNodeCode
                .append(countryCode)
                .append(newNodeId)
                .append(voltageLevelCode)
                .append(nodeCounter);

        UcteNodeCode nodeCode = getUcteNodeCode(newNodeCode.toString());
        ucteNodeIds.put(nodeId, nodeCode);
        return nodeCode;
    }


    /**
     * Generates a UCTE element identifier for a network branch element (line or transformer).
     * The element ID is composed of two node codes and an order code.
     *
     * <p>The generated ID follows UCTE format specifications:
     * [Node1Code][Node2Code][OrderCode]
     * where:
     * <ul>
     *   <li>Node1Code: UCTE code of the first terminal</li>
     *   <li>Node2Code: UCTE code of the second terminal</li>
     *   <li>OrderCode: Single character identifier</li>
     * </ul>
     *
     * @param branch the element instance of branch
     * @return the generated UCTE element identifier
     */
    private UcteElementId generateUcteElementId(Branch<?> branch) {

        String branchId = branch.getId();
        if (ucteElementIds.containsKey(branchId)) {
            return null;
        }

        UcteNodeCode node1 = ucteNodeIds.get(branch.getTerminal1().getBusBreakerView().getBus().getId());
        UcteNodeCode node2 = ucteNodeIds.get(branch.getTerminal2().getBusBreakerView().getBus().getId());

        char orderCode = branch instanceof TwoWindingsTransformer ?
                '1' :
                branch.getId().charAt(branch.getId().length() - 1);


        return new UcteElementId(node1, node2, orderCode);
    }

    /**
     * Extracts and converts a country code to UCTE format for a given voltage level.
     *
     * <p>The country code is determined through the following process:
     * <ol>
     *   <li>Attempts to get the country from the voltage level's substation</li>
     *   <li>Maps the country name to its corresponding UCTE code</li>
     *   <li>Returns 'X' as fallback if no valid mapping is found</li>
     * </ol>
     *
     * @param voltageLevel the voltage level from which to extract the country code
     * @return the UCTE country code character, or 'X' if no valid country is found)
     */
    private static char getCountryCode(VoltageLevel voltageLevel) {

        String country = voltageLevel.getSubstation()
                .map(s->s.getCountry()
                        .map(Country::getName)
                        .orElse("XX"))
                .orElse("XX");

        for (UcteCountryCode countryCode : UcteCountryCode.values()) {
            if (country.equalsIgnoreCase(countryCode.getPrettyName())) {
                return countryCode.getUcteCode();
            }
        }
        return 'X';
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
        return ucteNodeIds.computeIfAbsent(id, k -> UcteNodeCode.parseUcteNodeCode(k).orElseThrow(() -> new UcteException("Invalid UCTE node identifier: " + k)));
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
        return ucteElementIds.computeIfAbsent(id, k -> UcteElementId.parseUcteElementId(k).orElseThrow(() -> new UcteException("Invalid UCTE node identifier: " + k)));
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