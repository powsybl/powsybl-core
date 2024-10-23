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
     *
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

        // Process all substations and their buses
        // For each substation, get country name and convert all bus IDs to UCTE format
        network.getSubstationStream()
                .forEach(substation -> {
                    String countryCode = substation.getCountry()
                            .map(Country::getName)
                            .orElse("XX");

                    substation.getVoltageLevelStream()
                            .flatMap(vl -> StreamSupport.stream(vl.getBusBreakerView().getBuses().spliterator(), false))
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

    /**
     * Generates a UCTE node identifier for a given bus and stores it in the ucteNodeIds map.
     * The UCTE node format follows the pattern: C_NNNNN_V where:
     * - length : 8
     * - char[0] : country code
     * - char[1-5]: First 5 characters of bus ID (padded with '_' if shorter)
     * - char[6] : Voltage level code followed by
     * - char[7] : letter or figure for differentiating bus bars (optional) -> actually replace by '_'
     *
     * @param country The country name of the bus's substation
     * @param bus The bus for which to generate the UCTE node ID
     * @throws UcteException if the generated ID is not a valid UCTE node identifier
     */
    private void generateUcteNodeId(String country, Bus bus) {
        String busId = bus.getId();

        // Skip if this bus ID has already been processed
        if (ucteNodeIds.containsKey(busId)) {
            return;
        }
        // Initialize StringBuilder with fixed capacity of 8 for UCTE format
        StringBuilder nameBuilder = new StringBuilder(8);
        // get ucte country code with the country name
        nameBuilder.append(getCountryCode(country));
        // Format bus ID to fill chars[1-5]
        String fomatedId = busId.length() >= 5
                ? busId.substring(0, 5)
                : String.format("%-5s", busId).replace(' ', '_');
        nameBuilder.append(fomatedId);
        // Add voltage level code (char[6]) and trailing underscore (char[7])
        nameBuilder.append(getVoltageLevelCode(bus.getVoltageLevel().getNominalV())).append('_');
        String name = nameBuilder.toString();
        // Validate and store the generated UCTE node code
        UcteNodeCode nodeCode = UcteNodeCode.parseUcteNodeCode(name)
                .orElseThrow(() -> new UcteException("Invalid UCTE node identifier: " + name));
        ucteNodeIds.put(busId, nodeCode);
    }

    /**
     * Generates a UCTE element identifier for a given two-winding transformer and stores it in the ucteElementIds map.
     * The UCTE element format is constructed from the two connected buses: AAAAAAAA_BBBBBBBB_N where:
     * - AAAAAAAA: UCTE node ID of the first bus (8 chars)
     * - BBBBBBBB: UCTE node ID of the second bus (8 chars)
     * - N: Order code (actually 1 for transformers)
     * The original ID format is: busId1_busId2
     *
     * @param transformer The two-winding transformer for which to generate the UCTE element ID
     * @throws UcteException if the generated ID is not a valid UCTE element identifier
     */
    private void generateUcteElementId(TwoWindingsTransformer transformer) {

        // Get bus IDs from both terminals of the transformer
        String busId1 = transformer.getTerminal1().getBusBreakerView().getBus().getId();
        String busId2 = transformer.getTerminal2().getBusBreakerView().getBus().getId();

        // Create original ID by concatenating both bus IDs with underscore
        String originalId = new StringBuilder(busId1.length() + busId2.length() + 1)
                .append(busId1)
                .append('_')
                .append(busId2)
                .toString();

        // Skip if this transformer ID has already been processed
        if (ucteElementIds.containsKey(originalId)) {return;}

        // Create UCTE element ID using previously converted UCTE node IDs
        // '1' is used as order code for transformers
        UcteElementId elementId = new UcteElementId(ucteNodeIds.get(busId1), ucteNodeIds.get(busId2), '1');

        // Validate and store the generated UCTE element ID
        ucteElementIds.computeIfAbsent(originalId, k -> UcteElementId.parseUcteElementId(elementId.toString()).orElseThrow(() -> new UcteException("Invalid UCTE node identifier: " + k)));

    }

    /**
     * Generates a UCTE element identifier for a given line and stores it in the ucteElementIds map.
     * The UCTE element format is constructed from the two connected buses: AAAAAAAA_BBBBBBBB_N where:
     * - AAAAAAAA: UCTE node ID of the first bus (8 chars)
     * - BBBBBBBB: UCTE node ID of the second bus (8 chars)
     * - N: Order code
     * The original ID format is: busId1_busId2_orderCode
     *
     * @param line The two-winding transformer for which to generate the UCTE element ID
     * @throws UcteException if the generated ID is not a valid UCTE element identifier
     */
    private void generateUcteElementId(Line line) {

        // Get bus IDs from both terminals of the line ang get the orderCode (last char of the id)
        String busId1 = line.getTerminal1().getBusBreakerView().getBus().getId();
        String busId2 = line.getTerminal2().getBusBreakerView().getBus().getId();
        char orderCode = line.getId().charAt(line.getId().length() - 1);

        // Create original ID by concatenating both bus IDs and orderCode with underscore
        String originalId = new StringBuilder(busId1.length() + busId2.length() + 1)
                .append(busId1)
                .append('_')
                .append(busId2)
                .append('_')
                .append(orderCode)
                .toString();

        // Skip if this line ID has already been processed
        if (ucteElementIds.containsKey(originalId)) {return;}
        // Create UCTE element ID using previously converted UCTE node IDs
        UcteElementId elementId = new UcteElementId(ucteNodeIds.get(busId1), ucteNodeIds.get(busId2), orderCode);
        // Validate and store the generated UCTE element ID
        ucteElementIds.computeIfAbsent(originalId, k -> UcteElementId.parseUcteElementId(elementId.toString()).orElseThrow(() -> new UcteException("Invalid UCTE node identifier: " + k)));

    }

    /**
     * Retrieves the UCTE single-character country code from a full country name.
     * Searches through UcteCountryCode enum to find a matching country name (case-insensitive).
     *
     * @param code The full country name to convert
     * @return The single character UCTE country code, or 'X' if country is not found
     */
    private static char getCountryCode(String code) {
        for(UcteCountryCode countryCode : UcteCountryCode.values()) {
            if(code.equalsIgnoreCase(countryCode.getPrettyName())) {
                return countryCode.getUcteCode();
            }
        }
        return 'X';
    }

    /**
     * Retrieves the UCTE voltage level code based on a nominal voltage value.
     *
     * @param voltage The nominal voltage value in kV
     * @return A character representing the UCTE voltage level code ('0' to 'N')
     * @throws IllegalArgumentException if no matching voltage level code is found
     */
    public static char getVoltageLevelCode(double voltage) {
        for (UcteVoltageLevelCode code : UcteVoltageLevelCode.values()) {
            if (code.getVoltageLevel() == (int) voltage) {
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
