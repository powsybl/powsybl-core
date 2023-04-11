/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseFixDuplicatedIDs {

    public PsseFixDuplicatedIDs(PssePowerFlowModel model) {
        this.model = Objects.requireNonNull(model);
        warnings = new ArrayList<>();
    }

    public void fix() {
        fixDuplicatedIDs(model);
        if (!warnings.isEmpty()) {
            writeWarnings();
        }
    }

    private void writeWarnings() {
        LOGGER.warn("PSS/E Fix duplicated IDs ...");
        warnings.forEach(LOGGER::warn);
        LOGGER.warn("PSS/E Fix duplicated IDs end.");
    }

    private void fixDuplicatedIDs(PssePowerFlowModel model) {

        // fix duplicated IDs only for non transformers branchs and transformers at the moment
        fixDuplicatedIDsNonTransformerBranches(model.getNonTransformerBranches());
        fixDuplicatedIDsTransformers(model.getTransformers());
    }

    private void fixDuplicatedIDsNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches) {
        Map<String, List<IDs>> busesNonTransformerBranches = new HashMap<>();

        for (int i = 0; i < nonTransformerBranches.size(); i++) {
            PsseNonTransformerBranch nonTransformerBranch = nonTransformerBranches.get(i);
            addBusesMap(busesNonTransformerBranches, i, nonTransformerBranch.getI(), nonTransformerBranch.getJ(), nonTransformerBranch.getCkt());
        }

        Map<String, List<IDs>> duplicatedBusesLinks = getDuplicates(busesNonTransformerBranches);
        duplicatedBusesLinks.forEach((key, value) -> {
            List<IDs> fixedIDs = obtainFixedIDs(value);
            for (int i = 0; i < fixedIDs.size(); i++) {

                // fix the psse record and add warning
                PsseNonTransformerBranch nonTransformerBranch = nonTransformerBranches.get(fixedIDs.get(i).psseIndex);
                nonTransformerBranch.setCkt(fixedIDs.get(i).id);

                warnings.add(String.format("NonTransformerBranch: I %s J %d CKT %s fixed to ---> I %d J %d CKT %s",
                    nonTransformerBranch.getI(), nonTransformerBranch.getJ(), value.get(i).id,
                    nonTransformerBranch.getI(), nonTransformerBranch.getJ(), fixedIDs.get(i).id));
            }
        });
    }

    private void fixDuplicatedIDsTransformers(List<PsseTransformer> transformers) {
        Map<String, List<IDs>> busesTransformers = new HashMap<>();

        for (int i = 0; i < transformers.size(); i++) {
            PsseTransformer transformer = transformers.get(i);
            addBusesMap(busesTransformers, i, transformer.getI(), transformer.getJ(), transformer.getK(), transformer.getCkt());
        }

        Map<String, List<IDs>> duplicatedBusesLinks = getDuplicates(busesTransformers);
        duplicatedBusesLinks.forEach((key, value) -> {
            List<IDs> fixedIDs = obtainFixedIDs(value);
            for (int i = 0; i < fixedIDs.size(); i++) {

                // fix the psse record and add warning
                PsseTransformer transformer = transformers.get(fixedIDs.get(i).psseIndex);
                transformer.setCkt(fixedIDs.get(i).id);

                warnings.add(String.format("Transformer: I %s J %d K %d CKT %s fixed to ---> I %d J %d K %d CKT %s",
                    transformer.getI(), transformer.getJ(), transformer.getK(), value.get(i).id,
                    transformer.getI(), transformer.getJ(), transformer.getK(), fixedIDs.get(i).id));
            }
        });
    }

    private static void addBusesMap(Map<String, List<IDs>> busesMap, int psseIndex, int busI, int busJ, String ckt) {
        String busString;
        if (busI < busJ) {
            busString = String.format("%06d-%06d", busI, busJ);
        } else {
            busString = String.format("%06d-%06d", busJ, busI);
        }

        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(new IDs(psseIndex, ckt));
    }

    private static void addBusesMap(Map<String, List<IDs>> busesMap, int psseIndex, int busI, int busJ, int busK, String ckt) {
        String busString;
        List<Integer> buses = new ArrayList<>();
        buses.add(busI);
        buses.add(busJ);
        buses.add(busK);
        Collections.sort(buses);
        busString = String.format("%06d-%06d-%06d", buses.get(0), buses.get(1), buses.get(2));

        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(new IDs(psseIndex, ckt));
    }

    private static List<IDs> obtainFixedIDs(List<IDs> iDs) {
        List<IDs> fixedIDs = new ArrayList<>();
        iDs.forEach(iD -> fixedIDs.add(obtainFixedID(iD, fixedIDs)));
        return fixedIDs;
    }

    private static IDs obtainFixedID(IDs iD, List<IDs> fixedIDs) {
        if (fixedIDs.isEmpty()) {
            return iD;
        }
        String firstCharacterString = iD.id.isEmpty() ? "0" : iD.id.substring(0, 1);
        String newId = obtainFixedId(iD.id, firstCharacterString, fixedIDs);
        return new IDs(iD.psseIndex, newId);
    }

    private static Map<String, List<IDs>> getDuplicates(Map<String, List<IDs>> busesMap) {
        Map<String, List<IDs>> duplicatedBusMap = new HashMap<>();

        busesMap.forEach((key, value) -> value.stream().collect(Collectors.groupingBy(s -> s.id))
            .entrySet()
            .stream()
            .filter(e -> e.getValue().size() > 1)
            .forEach(e -> duplicatedBusMap.put(key, e.getValue())));

        return duplicatedBusMap;
    }

    private static String obtainFixedId(String id, String firstCharacterString, List<IDs> fixedIDs) {
        for (char c = '0'; c <= '9'; c++) {
            String secondCharacterString = String.valueOf(c);
            if (isUsed(secondCharacterString, fixedIDs)) {
                continue;
            }
            return firstCharacterString + secondCharacterString;
        }
        // only lowercase are considered. It is enough
        for (char c = 'a'; c <= 'z'; c++) {
            String secondCharacterString = String.valueOf(c);
            if (isUsed(secondCharacterString, fixedIDs)) {
                continue;
            }
            return firstCharacterString + secondCharacterString;
        }
        return id;
    }

    private static boolean isUsed(String secondCharacterString, List<IDs> fixedIDs) {
        for (IDs iD : fixedIDs) {
            if (iD.id.length() > 1 && iD.id.substring(1, 2).equals(secondCharacterString)) {
                return true;
            }
        }
        return false;
    }

    private static final class IDs {
        private final int psseIndex;
        private final String id;

        IDs(int psseIndex, String id) {
            this.psseIndex = psseIndex;
            this.id = id;
        }
    }

    private final PssePowerFlowModel model;
    private final List<String> warnings;
    private static final Logger LOGGER = LoggerFactory.getLogger(PsseFixDuplicatedIDs.class);
}
