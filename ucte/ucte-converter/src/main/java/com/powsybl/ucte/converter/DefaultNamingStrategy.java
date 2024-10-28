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

import java.util.*;
import java.util.stream.StreamSupport;

/**
 * A {@link NamingStrategy} implementation that ensures the conformity of IDs with the UCTE-DEF format
 *
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
@AutoService(NamingStrategy.class)
public class DefaultNamingStrategy implements NamingStrategy {

    private final Map<String, UcteNodeCode> ucteNodeIds = new HashMap<>();
    private final Map<String, UcteElementId> ucteElementIds = new HashMap<>();

    @Override
    public String getName() {
        return "Default";
    }

    /**
     * Initializes the network by converting network elements names to UCTE format if needed.
     * @param network The network to check
     */
    @Override
    public void init(Network network) {
        //add condition to test network is already ucte format
        convertToUcte(network);
    }

    /**
     * Converts all network elements IDs to UCTE format. This includes:
     * - Converting bus IDs using country codes from their substations
     * - Converting line IDs
     * - Converting two-winding transformer IDs
     * The conversion process:
     * 1. For each substation, retrieves its country code (defaults to "XX" if not found)
     * 2. Processes all buses within the substation's voltage levels
     * 3. Processes all lines in the network
     * 4. Processes all two-winding transformers
     *
     * @param network The network whose elements need to be converted to UCTE format
     */
    private void convertToUcte(Network network) {
        network.getVoltageLevelStream()
                .forEach(voltageLevel -> {
                    // Get country code from substation if exists, else use default
                    String countryCode = voltageLevel.getSubstation()
                            .map(s -> s.getCountry().map(Country::getName).orElse("XX"))
                            .orElse("XX");

                    StreamSupport.stream(voltageLevel.getBusBreakerView().getBuses().spliterator(), false)
                            .forEach(bus -> {
                                generateUcteNodeId(countryCode, bus);
                                System.out.println("BUS : " + bus.getId() + "--> " + ucteNodeIds.get(bus.getId()));
                            });
                });

        // Convert all line IDs to UCTE format
        network.getLineStream()
                .forEach(line -> {
                    generateUcteElementId(line);
                    System.out.println("LINE : " + line.getId() + "--> " + ucteElementIds.get(line.getId()));
                });

        // Convert all two-winding transformer IDs to UCTE format
        network.getTwoWindingsTransformerStream()
                .forEach(transformer -> {
                    generateUcteElementId(transformer);
                    System.out.println("TwoWindingsTrans : " + transformer.getId() + "--> " + ucteElementIds.get(transformer.getId()));
                });
    }

    private void generateUcteNodeId(String country, Bus bus) {
        String id = bus.getId();
        if (ucteElementIds.containsKey(id)) {return;}
        char countryCode = getCountryCode(country);
        char voltageLevelCode = getVoltageLevelCode(bus.getVoltageLevel().getNominalV());
        String formattedId = formatUniqueId(id);
        UcteNodeCode nodeCode = UcteNodeCode.convertToUcteNodeId(countryCode, voltageLevelCode, formattedId).get();
        ucteNodeIds.put(id, nodeCode);
    }

    private String formatUniqueId(String id) {
        /*
            id = "NHV1_NHV2_1_1_BUS";
            1. parts = ["NHV1", "NHV2", "1", "1", "BUS"]
            2. unique = "NH" (2 first char)
            3. numbers = "1121" -> unique = "NH1" (last number)
            4. lastPart = "BUS" -> unique = "NH1BU" (2 first char)
            5. length = 5, else complete with _
            result= "NH1BU"
         */
        String[] parts = id.split("_");
        StringBuilder unique = new StringBuilder();
        if (parts.length > 1) {
            unique.append(parts[0].substring(0, 2));
            String numbers = id.replaceAll("[^0-9]", "");
            if (!numbers.isEmpty()) {
                unique.append(numbers.charAt(numbers.length() - 1));
            }
            String lastPart = parts[parts.length - 1].replaceAll("[0-9]", "");
            if (lastPart.length() > 0) {
                unique.append(lastPart.substring(0, Math.min(2, lastPart.length())));
            }
        } else {
            unique.append(id.substring(0, Math.min(5, id.length())));
        }
        while (unique.length() < 5) {
            unique.append('_');
        }
        return unique.substring(0, 5);
    }

    private void generateUcteElementId(String originalId, UcteNodeCode node1, UcteNodeCode node2, char orderCode) {
        UcteElementId ucteElementId = new UcteElementId(node1, node2, orderCode);
        ucteElementIds.put(originalId, ucteElementId);
    }

    private void generateUcteElementId(TwoWindingsTransformer transformer) {
        String transformerId = transformer.getId();
        if (ucteElementIds.containsKey(transformerId)) {
            return;
        }
        UcteNodeCode node1 = ucteNodeIds.get(transformer.getTerminal1().getBusBreakerView().getBus().getId());
        UcteNodeCode node2 = ucteNodeIds.get(transformer.getTerminal2().getBusBreakerView().getBus().getId());
        generateUcteElementId(transformerId, node1, node2, '1');
    }

    private void generateUcteElementId(Line line) {
        String lineId = line.getId();
        // Skip if this line ID has already been processed
        if (ucteElementIds.containsKey(lineId)) {return;}
        UcteNodeCode node1 = ucteNodeIds.get(line.getTerminal1().getBusBreakerView().getBus().getId());
        UcteNodeCode node2 = ucteNodeIds.get(line.getTerminal2().getBusBreakerView().getBus().getId());
        char orderCode = line.getId().charAt(line.getId().length() - 1);
        generateUcteElementId(lineId,node1, node2, orderCode);
    }


    private static char getCountryCode(String code) {
        for (UcteCountryCode countryCode : UcteCountryCode.values()) {
            if (code.equalsIgnoreCase(countryCode.getPrettyName())) {
                return countryCode.getUcteCode();
            }
        }
        return 'X';
    }



    public static char getVoltageLevelCode(double voltage) {
        //If voltage level neither than min UCTEVoltageLevel
        if((int)voltage < UcteVoltageLevelCode.VL_27.getVoltageLevel()){
            return '7';
        }
        //If voltage level gather than min UCTEVoltageLevel
        if((int)voltage > UcteVoltageLevelCode.VL_750.getVoltageLevel()){
            return '0';
        }
        for (UcteVoltageLevelCode code : UcteVoltageLevelCode.values()) {
            if ((int) voltage >= code.getVoltageLevel()-10 && (int) voltage <= code.getVoltageLevel()+10) {
                return (char) ('0' + code.ordinal());
            }
        }
        throw new IllegalArgumentException("No voltage level code found for " + voltage + " kV");
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