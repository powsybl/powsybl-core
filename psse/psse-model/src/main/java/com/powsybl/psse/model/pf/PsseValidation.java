/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.psse.model.PsseVersion;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseValidation {

    private final List<String> warnings;
    private boolean validCase;
    private static final Logger LOGGER = LoggerFactory.getLogger(PsseValidation.class);

    private static final String WARNING_TRANSFORMER_1_PARAMETER = "Transformer: %s Unexpected %s: %.5f";
    private static final String WARNING_TRANSFORMER_2_PARAMETERS = "Transformer: %s Unexpected %s: %.5f %.5f";

    public PsseValidation(PssePowerFlowModel model, PsseVersion psseVersion) {
        Objects.requireNonNull(model);
        warnings = new ArrayList<>();
        validCase = true;

        validate(model, psseVersion);
        if (!validCase) {
            writeWarnings();
        }
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isValidCase() {
        return validCase;
    }

    private void writeWarnings() {
        LOGGER.warn("PSS/E Validation ...");
        warnings.forEach(LOGGER::warn);
        LOGGER.warn("PSS/E Validation end. ValidCase {}", validCase);
    }

    private void validate(PssePowerFlowModel model, PsseVersion psseVersion) {

        Map<Integer, List<Integer>> buses = generateBuses(model.getBuses());

        validateCaseIdentification(model.getCaseIdentification());
        validateBuses(model.getBuses(), buses);
        validateLoads(model.getLoads(), buses);
        validateFixedShunts(model.getFixedShunts(), buses);
        validateGenerators(model.getBuses(), model.getGenerators(), buses);
        validateNonTransformerBranches(model.getNonTransformerBranches(), buses);
        validateTransformers(model.getTransformers(), buses);
        validateTwoTerminalDcTransmissionLines(model.getTwoTerminalDcTransmissionLines(), buses);
        validateVscDcTransmissionLines(model.getVoltageSourceConverterDcTransmissionLines(), buses, psseVersion);

        validateFactsDevices(model.getFacts(), buses, psseVersion);
        validateSwitchedShunts(model.getSwitchedShunts(), buses, psseVersion);
    }

    private static Map<Integer, List<Integer>> generateBuses(List<PsseBus> psseBuses) {
        Map<Integer, List<Integer>> buses = new HashMap<>();
        for (int i = 0; i < psseBuses.size(); i++) {
            buses.computeIfAbsent(psseBuses.get(i).getI(), k -> new ArrayList<>()).add(i);
        }
        return buses;
    }

    private void validateCaseIdentification(PsseCaseIdentification caseIdentification) {
        if (caseIdentification.getSbase() <= 0.0) {
            warnings.add(String.format(Locale.US, "CaseIdentification: Unexpected Sbase: %.2f", caseIdentification.getSbase()));
            validCase = false;
        }
        if (caseIdentification.getBasfrq() <= 0.0) {
            warnings.add(String.format(Locale.US, "CaseIdentification: Unexpected Basfrq: %.2f", caseIdentification.getBasfrq()));
            validCase = false;
        }
    }

    private void validateBuses(List<PsseBus> psseBuses, Map<Integer, List<Integer>> buses) {
        for (Map.Entry<Integer, List<Integer>> entry : buses.entrySet()) {
            if (entry.getValue().size() != 1) {
                warnings.add(String.format("Bus: %d defined multiple times (%d)", entry.getKey(), entry.getValue().size()));
                validCase = false;
            }
        }

        for (PsseBus psseBus : psseBuses) {
            if (psseBus.getI() < 1 || psseBus.getI() > 999997) {
                warnings.add(String.format("Bus: Unexpected I: %d", psseBus.getI()));
                validCase = false;
            }
            if (psseBus.getBaskv() < 0.0) {
                warnings.add(String.format(Locale.US, "Bus: %d Unexpected Baskv: %.2f", psseBus.getI(), psseBus.getBaskv()));
                validCase = false;
            }
        }
    }

    private void validateLoads(List<PsseLoad> loads, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesLoads = new HashMap<>();

        for (PsseLoad load : loads) {
            if (!buses.containsKey(load.getI())) {
                warnings.add(String.format("Load: Unexpected I: %d", load.getI()));
                validCase = false;
            }
            addBusesMap(busesLoads, load.getI(), load.getId());
        }

        checkDuplicates("Load", "loads", getDuplicates(busesLoads));
    }

    private void validateFixedShunts(List<PsseFixedShunt> fixedShunts, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesFixedShunts = new HashMap<>();

        for (PsseFixedShunt fixedShunt : fixedShunts) {
            if (!buses.containsKey(fixedShunt.getI())) {
                warnings.add(String.format("FixedShunt: Unexpected I: %d", fixedShunt.getI()));
                validCase = false;
            }
            addBusesMap(busesFixedShunts, fixedShunt.getI(), fixedShunt.getId());
        }

        checkDuplicates("FixedShunt", "fixed shunts", getDuplicates(busesFixedShunts));
    }

    private void validateGenerators(List<PsseBus> psseBuses, List<PsseGenerator> generators, Map<Integer, List<Integer>> buses) {

        Map<String, List<String>> busesGenerators = new HashMap<>();

        for (PsseGenerator generator : generators) {
            if (!buses.containsKey(generator.getI())) {
                warnings.add(String.format("Generator: Unexpected I: %d", generator.getI()));
                validCase = false;
            }
            if (generator.getQt() < generator.getQb()) {
                warnings.add(String.format(Locale.US, "Generator: %d %s Unexpected Qmin: %.2f Qmax: %.2f", generator.getI(), generator.getId(), generator.getQb(), generator.getQt()));
                validCase = false;
            }
            if (generator.getIreg() != 0 && !buses.containsKey(generator.getIreg())) {
                warnings.add(String.format("Generator: %d %s Unexpected IReg: %d", generator.getI(), generator.getId(), generator.getIreg()));
                validCase = false;
            }
            if (generator.getPt() < generator.getPb()) {
                warnings.add(String.format(Locale.US, "Generator: %d %s Unexpected Pmin: %.2f Pmax: %.2f", generator.getI(), generator.getId(), generator.getPb(), generator.getPt()));
                validCase = false;
            }
            validateGeneratorRegulatingBus(psseBuses, buses, generator);

            addBusesMap(busesGenerators, generator.getI(), generator.getId());
        }

        checkDuplicates("Generator", "generators", getDuplicates(busesGenerators));
    }

    private void validateGeneratorRegulatingBus(List<PsseBus> psseBuses, Map<Integer, List<Integer>> buses, PsseGenerator generator) {
        PsseBus regulatingBus = getRegulatingBus(psseBuses, buses, generator.getIreg(), generator.getI());
        if (regulatingBus != null
            && (regulatingBus.getIde() == 2 || regulatingBus.getIde() == 3)
            && generator.getVs() <= 0.0) {
            warnings.add(String.format(Locale.US, "Generator: %d %s Unexpected Voltage setpoint: %.2f", generator.getI(), generator.getId(), generator.getVs()));
            validCase = false;
        }
    }

    private void validateNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesNonTransformerBranches = new HashMap<>();

        for (PsseNonTransformerBranch nonTransformerBranch : nonTransformerBranches) {
            if (!buses.containsKey(nonTransformerBranch.getI())) {
                warnings.add(String.format("NonTransformerBranch: Unexpected I: %d", nonTransformerBranch.getI()));
                validCase = false;
            }
            if (!buses.containsKey(nonTransformerBranch.getJ())) {
                warnings.add(String.format("NonTransformerBranch: Unexpected J: %d", nonTransformerBranch.getJ()));
                validCase = false;
            }
            if (nonTransformerBranch.getX() == 0.0) {
                warnings.add(String.format(Locale.US, "NonTransformerBranch: %d %d %s Unexpected X: %.5f", nonTransformerBranch.getI(), nonTransformerBranch.getJ(), nonTransformerBranch.getCkt(), nonTransformerBranch.getX()));
                validCase = false;
            }
            addBusesMap(busesNonTransformerBranches, nonTransformerBranch.getI(), nonTransformerBranch.getJ(), nonTransformerBranch.getCkt());
        }

        checkDuplicatesLinks("NonTransformerBranch", "branches", getDuplicates(busesNonTransformerBranches));
    }

    private void validateTransformers(List<PsseTransformer> transformers, Map<Integer, List<Integer>> buses) {
        List<PsseTransformer> twoWindingsTransformers = transformers.parallelStream()
            .filter(transformer -> transformer.getK() == 0).toList();
        validateTwoWindingsTransformers(twoWindingsTransformers, buses);

        List<PsseTransformer> threeWindingsTransformers = transformers.parallelStream()
            .filter(transformer -> transformer.getK() != 0).toList();
        validateThreeWindingsTransformers(threeWindingsTransformers, buses);
    }

    private void validateTwoWindingsTransformers(List<PsseTransformer> transformers, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesTransformers = new HashMap<>();

        for (PsseTransformer transformer : transformers) {
            validateTransformerBus(buses, transformer.getI(), "I");
            validateTransformerBus(buses, transformer.getJ(), "J");

            String id = String.format("%d %d %s", transformer.getI(), transformer.getJ(), transformer.getCkt());
            validateTransformerX(id, transformer.getX12(), "X12");
            validateTransformerRatio(id, transformer.getWinding1().getWindv(), "ratio");
            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase12(), "sbase12");
            validateTransformerWindingVmiVma(id, transformer.getWinding1().getCod(), transformer.getWinding1().getVmi(), transformer.getWinding1().getVma(), "winding1 Vmi Vma");
            validateTransformerWindingRmiRma(id, transformer.getWinding1().getCod(), transformer.getWinding1().getRmi(), transformer.getWinding1().getRma(), "winding1 Rmi Rma");
            validateTransformerWindingCont(buses, id, transformer.getWinding1().getCod(), transformer.getWinding1().getCont(), "winding1 Cont");

            addBusesMap(busesTransformers, transformer.getI(), transformer.getJ(), transformer.getCkt());
        }

        checkDuplicatesLinks("Transformer", "branches", getDuplicates(busesTransformers));
    }

    private void validateThreeWindingsTransformers(List<PsseTransformer> transformers, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesTransformers = new HashMap<>();

        for (PsseTransformer transformer : transformers) {
            validateTransformerBus(buses, transformer.getI(), "I");
            validateTransformerBus(buses, transformer.getJ(), "J");
            validateTransformerBus(buses, transformer.getK(), "K");

            String id = String.format("%d %d %d %s", transformer.getI(), transformer.getJ(), transformer.getK(), transformer.getCkt());
            validateTransformerX(id, transformer.getX12(), "X12");
            validateTransformerX(id, transformer.getX31(), "X31");
            validateTransformerX(id, transformer.getX23(), "X23");

            validateTransformerRatio(id, transformer.getWinding1().getWindv(), "winding1 ratio");
            validateTransformerRatio(id, transformer.getWinding2().getWindv(), "winding2 ratio");
            validateTransformerRatio(id, transformer.getWinding3().getWindv(), "winding3 ratio");

            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase12(), "sbase12");
            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase23(), "sbase23");
            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase31(), "sbase31");

            validateTransformerWindingVmiVma(id, transformer.getWinding1().getCod(), transformer.getWinding1().getVmi(), transformer.getWinding1().getVma(), "winding1 Vmi Vma");
            validateTransformerWindingVmiVma(id, transformer.getWinding2().getCod(), transformer.getWinding2().getVmi(), transformer.getWinding2().getVma(), "winding2 Vmi Vma");
            validateTransformerWindingVmiVma(id, transformer.getWinding3().getCod(), transformer.getWinding3().getVmi(), transformer.getWinding3().getVma(), "winding3 Vmi Vma");

            validateTransformerWindingRmiRma(id, transformer.getWinding1().getCod(), transformer.getWinding1().getRmi(), transformer.getWinding1().getRma(), "winding1 Rmi Rma");
            validateTransformerWindingRmiRma(id, transformer.getWinding2().getCod(), transformer.getWinding2().getRmi(), transformer.getWinding2().getRma(), "winding2 Rmi Rma");
            validateTransformerWindingRmiRma(id, transformer.getWinding3().getCod(), transformer.getWinding3().getRmi(), transformer.getWinding3().getRma(), "winding3 Rmi Rma");

            validateTransformerWindingCont(buses, id, transformer.getWinding1().getCod(), transformer.getWinding1().getCont(), "winding1 Cont");
            validateTransformerWindingCont(buses, id, transformer.getWinding2().getCod(), transformer.getWinding2().getCont(), "winding2 Cont");
            validateTransformerWindingCont(buses, id, transformer.getWinding3().getCod(), transformer.getWinding3().getCont(), "winding3 Cont");

            addBusesMap(busesTransformers, transformer.getI(), transformer.getJ(), transformer.getK(), transformer.getCkt());
        }

        Map<String, List<String>> duplicatedBusesTransformers = getDuplicates(busesTransformers);
        if (!duplicatedBusesTransformers.isEmpty()) {
            duplicatedBusesTransformers.forEach((key,
                value) -> warnings.add(String.format(
                    "Transformer: Multiple branches (%d) between buses %d, %d and %d with the same Id %s",
                    value.size(), firstBus(key), secondBus(key), thirdBus(key), value.get(0))));
            validCase = false;
        }
    }

    private void validateTransformerBus(Map<Integer, List<Integer>> buses, int bus, String busTag) {
        if (!buses.containsKey(bus)) {
            warnings.add(String.format("Transformer: Unexpected %s: %d", busTag, bus));
            validCase = false;
        }
    }

    private void validateTransformerX(String id, double x, String xTag) {
        if (x == 0.0) {
            warnings.add(getWarningTransformer1Parameter(id, xTag, x));
            validCase = false;
        }
    }

    private void validateTransformerRatio(String id, double ratio, String ratioTag) {
        if (ratio <= 0.0) {
            warnings.add(getWarningTransformer1Parameter(id, ratioTag, ratio));
            validCase = false;
        }
    }

    private void validateTransformerSbase(String id, int cz, int cm, double sbase, String sbaseTag) {
        if ((cz == 2 || cz == 3 || cm == 2) && sbase <= 0.0) {
            warnings.add(getWarningTransformer1Parameter(id, sbaseTag, sbase));
            validCase = false;
        }
    }

    private void validateTransformerWindingVmiVma(String id, int cod, double windingVmi, double windingVma, String windingVmiVmaTag) {
        if (Math.abs(cod) == 1 && (windingVmi <= 0.0 || windingVma <= 0.0 || windingVma < windingVmi)) {
            warnings.add(getWarningTransformer2Parameters(id, windingVmiVmaTag, windingVmi, windingVma));
            validCase = false;
        }
        if ((Math.abs(cod) == 2 || Math.abs(cod) == 3 || Math.abs(cod) == 5) && windingVma < windingVmi) {
            warnings.add(getWarningTransformer2Parameters(id, windingVmiVmaTag, windingVmi, windingVma));
            validCase = false;
        }
    }

    private void validateTransformerWindingRmiRma(String id, int cod, double windingRmi, double windingRma, String windingRmiRmaTag) {
        if ((Math.abs(cod) == 1 || Math.abs(cod) == 2) && (windingRmi <= 0.0 || windingRma <= 0.0 || windingRma < windingRmi)) {
            warnings.add(getWarningTransformer2Parameters(id, windingRmiRmaTag, windingRmi, windingRma));
            validCase = false;
        }
        if ((Math.abs(cod) == 3 || Math.abs(cod) == 5) && windingRma < windingRmi) {
            warnings.add(getWarningTransformer2Parameters(id, windingRmiRmaTag, windingRmi, windingRma));
            validCase = false;
        }
    }

    private void validateTransformerWindingCont(Map<Integer, List<Integer>> buses, String id, int cod, int windingCont, String windingContTag) {
        if (Math.abs(cod) == 1 && (windingCont == 0 || !buses.containsKey(Math.abs(windingCont)))) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %d", id, windingContTag, windingCont));
            validCase = false;
        }
    }

    private void validateTwoTerminalDcTransmissionLines(List<PsseTwoTerminalDcTransmissionLine> twoTerminalDcTransmissionLines, Map<Integer, List<Integer>> buses) {
        Map<String, Integer> twoTerminalDcNames = new HashMap<>();
        for (PsseTwoTerminalDcTransmissionLine twoTerminalDc : twoTerminalDcTransmissionLines) {
            if (!buses.containsKey(twoTerminalDc.getRectifier().getIp())) {
                warnings.add(String.format("TwoTerminalDcTransmissionLine: %s Unexpected rectifier Ip: %d", twoTerminalDc.getName(), twoTerminalDc.getRectifier().getIp()));
                validCase = false;
            }
            if (!buses.containsKey(twoTerminalDc.getInverter().getIp())) {
                warnings.add(String.format("TwoTerminalDcTransmissionLine: %s Unexpected inverter Ip: %d", twoTerminalDc.getName(), twoTerminalDc.getInverter().getIp()));
                validCase = false;
            }
            twoTerminalDcNames.put(twoTerminalDc.getName(), twoTerminalDcNames.getOrDefault(twoTerminalDc.getName(), 0) + 1);
        }
        List<String> duplicatedNames = twoTerminalDcNames.keySet().stream().filter(key -> twoTerminalDcNames.get(key) > 1).toList();
        if (!duplicatedNames.isEmpty()) {
            duplicatedNames.forEach(name -> warnings.add(String.format("TwoTerminalDcTransmissionLine: This name %s is not unique", name)));
            validCase = false;
        }
    }

    private void validateVscDcTransmissionLines(List<PsseVoltageSourceConverterDcTransmissionLine> vscDcTransmissionLines, Map<Integer, List<Integer>> buses, PsseVersion psseVersion) {
        Map<String, Integer> vscDcTransmissionLinesNames = new HashMap<>();
        for (PsseVoltageSourceConverterDcTransmissionLine vscDcTransmissionLine : vscDcTransmissionLines) {
            if (!buses.containsKey(vscDcTransmissionLine.getConverter1().getIbus())) {
                warnings.add(String.format("VoltageSourceConverterDcTransmissionLine: %s Unexpected converter1 Ibus: %d", vscDcTransmissionLine.getName(), vscDcTransmissionLine.getConverter1().getIbus()));
                validCase = false;
            }
            if (!buses.containsKey(vscDcTransmissionLine.getConverter2().getIbus())) {
                warnings.add(String.format("VoltageSourceConverterDcTransmissionLine: %s Unexpected converter2 Ibus: %d", vscDcTransmissionLine.getName(), vscDcTransmissionLine.getConverter2().getIbus()));
                validCase = false;
            }
            if (vscDcTransmissionLineRegulatingBus(vscDcTransmissionLine.getConverter1(), psseVersion) != 0 && !buses.containsKey(vscDcTransmissionLineRegulatingBus(vscDcTransmissionLine.getConverter1(), psseVersion))) {
                warnings.add(String.format("VoltageSourceConverterDcTransmissionLine: %s Unexpected converter1 Vsreg bus: %d", vscDcTransmissionLine.getName(), vscDcTransmissionLineRegulatingBus(vscDcTransmissionLine.getConverter1(), psseVersion)));
                validCase = false;
            }
            if (vscDcTransmissionLineRegulatingBus(vscDcTransmissionLine.getConverter2(), psseVersion) != 0 && !buses.containsKey(vscDcTransmissionLineRegulatingBus(vscDcTransmissionLine.getConverter2(), psseVersion))) {
                warnings.add(String.format("VoltageSourceConverterDcTransmissionLine: %s Unexpected converter2 Vsreg bus: %d", vscDcTransmissionLine.getName(), vscDcTransmissionLineRegulatingBus(vscDcTransmissionLine.getConverter2(), psseVersion)));
                validCase = false;
            }
            vscDcTransmissionLinesNames.put(vscDcTransmissionLine.getName(), vscDcTransmissionLinesNames.getOrDefault(vscDcTransmissionLine.getName(), 0) + 1);
        }
        List<String> duplicatedNames = vscDcTransmissionLinesNames.keySet().stream().filter(key -> vscDcTransmissionLinesNames.get(key) > 1).toList();
        if (!duplicatedNames.isEmpty()) {
            duplicatedNames.forEach(name -> warnings.add(String.format("VoltageSourceConverterDcTransmissionLine: This name %s is not unique", name)));
            validCase = false;
        }
    }

    private static int vscDcTransmissionLineRegulatingBus(PsseVoltageSourceConverter converter, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return converter.getVsreg();
        } else {
            return converter.getRemot();
        }
    }

    private void validateFactsDevices(List<PsseFacts> factsDevices, Map<Integer, List<Integer>> buses, PsseVersion psseVersion) {
        Map<String, Integer> factsDevicesNames = new HashMap<>();

        // Only bus I is validated as only statcom facts devices are considered
        for (PsseFacts factsDevice : factsDevices) {
            if (!buses.containsKey(factsDevice.getI())) {
                warnings.add(String.format("FactsDevice: %s Unexpected I: %d", factsDevice.getName(), factsDevice.getI()));
                validCase = false;
            }
            int regulatingBus = factsDeviceRegulatingBus(factsDevice, psseVersion);
            if (regulatingBus != 0 && !buses.containsKey(regulatingBus)) {
                warnings.add(String.format("FactsDevice: %s Unexpected remot/fcreg: %d", factsDevice.getName(), regulatingBus));
                validCase = false;
            }
            factsDevicesNames.put(factsDevice.getName(), factsDevicesNames.getOrDefault(factsDevice.getName(), 0) + 1);
        }

        List<String> duplicatedNames = factsDevicesNames.keySet().stream().filter(key -> factsDevicesNames.get(key) > 1).toList();
        if (!duplicatedNames.isEmpty()) {
            duplicatedNames.forEach(name -> warnings.add(String.format("FactsDevices: This name %s is not unique", name)));
            validCase = false;
        }
    }

    private static int factsDeviceRegulatingBus(PsseFacts factsDevice, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return factsDevice.getFcreg();
        } else {
            return factsDevice.getRemot();
        }
    }

    private void validateSwitchedShunts(List<PsseSwitchedShunt> switchedShunts, Map<Integer, List<Integer>> buses, PsseVersion psseVersion) {
        Map<String, List<String>> busesSwitchedShunts = new HashMap<>();

        for (PsseSwitchedShunt switchedShunt : switchedShunts) {
            if (!buses.containsKey(switchedShunt.getI())) {
                warnings.add(String.format("SwitchedShunt: Unexpected I: %d", switchedShunt.getI()));
                validCase = false;
            }
            String id = switchedShuntId(switchedShunt, psseVersion);
            int regulatingBus = switchedShuntRegulatingBus(switchedShunt, psseVersion);
            if (switchedShunt.getModsw() != 0 && regulatingBus != 0 && !buses.containsKey(regulatingBus)) {
                warnings.add(String.format("SwitchedShunt: %s Unexpected Swrem/Swreg: %d", id, regulatingBus));
                validCase = false;
            }
            if (switchedShunt.getModsw() != 0 && switchedShunt.getVswhi() < switchedShunt.getVswlo()) {
                warnings.add(String.format(Locale.US, "SwitchedShunt: %s Unexpected Vswlo Vswhi: %.5f %.5f", id, switchedShunt.getVswlo(), switchedShunt.getVswhi()));
                validCase = false;
            }
            if ((switchedShunt.getModsw() == 1 || switchedShunt.getModsw() == 2) && (switchedShunt.getVswlo() <= 0.0 || switchedShunt.getVswhi() <= 0.0)) {
                warnings.add(String.format(Locale.US, "SwitchedShunt: %s Unexpected Vswlo Vswhi: %.5f %.5f", id, switchedShunt.getVswlo(), switchedShunt.getVswhi()));
                validCase = false;
            }
            addSwitchedShuntBusesMap(busesSwitchedShunts, switchedShunt, psseVersion);
        }

        Map<String, List<String>> duplicatedBusesSwitchedShunts = getDuplicates(busesSwitchedShunts);
        if (!duplicatedBusesSwitchedShunts.isEmpty()) {
            duplicatedBusesSwitchedShunts.forEach((key, value) -> warnings.add(multipleSwitchedShuntString(key, value, psseVersion)));
            validCase = false;
        }
    }

    private static String switchedShuntId(PsseSwitchedShunt switchedShunt, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return String.format("%d %s", switchedShunt.getI(), switchedShunt.getId());
        } else {
            return String.format("%d", switchedShunt.getI());
        }
    }

    private static int switchedShuntRegulatingBus(PsseSwitchedShunt switchedShunt, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return switchedShunt.getSwreg();
        } else {
            return switchedShunt.getSwrem();
        }
    }

    private static void addSwitchedShuntBusesMap(Map<String, List<String>> busesSwitchedShunts, PsseSwitchedShunt switchedShunt, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            addBusesMap(busesSwitchedShunts, switchedShunt.getI(), switchedShunt.getId());
        } else {
            addBusesMap(busesSwitchedShunts, switchedShunt.getI(), "1");
        }
    }

    private static String multipleSwitchedShuntString(String key, List<String> value, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return String.format("SwitchedShunt: Multiple fixed shunts (%d) at bus %d with the same Id %s", value.size(), Integer.valueOf(key), value.get(0));
        } else {
            return String.format("SwitchedShunt: Multiple fixed shunts (%d) at bus %d", value.size(), Integer.valueOf(key));
        }
    }

    private static PsseBus getRegulatingBus(List<PsseBus> psseBuses, Map<Integer, List<Integer>> buses, int ireg, int i) {
        int regulatingId = i;
        if (ireg != 0) {
            regulatingId = ireg;
        }

        if (buses.containsKey(regulatingId)) {
            return psseBuses.get(buses.get(regulatingId).get(0));
        }

        return null;
    }

    private static Map<String, List<String>> getDuplicates(Map<String, List<String>> busesMap) {
        Map<String, List<String>> duplicatedBusMap = new HashMap<>();

        busesMap.forEach((key, value) -> value.stream().collect(Collectors.groupingBy(s -> s))
            .entrySet()
            .stream()
            .filter(e -> e.getValue().size() > 1)
            .forEach(e -> duplicatedBusMap.put(key, e.getValue())));

        return duplicatedBusMap;
    }

    private static void addBusesMap(Map<String, List<String>> busesMap, int busI, String id) {
        String busString = String.format("%06d", busI);
        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(id);
    }

    private static void addBusesMap(Map<String, List<String>> busesMap, int busI, int busJ, String ckt) {
        String busString;
        if (busI < busJ) {
            busString = String.format("%06d-%06d", busI, busJ);
        } else {
            busString = String.format("%06d-%06d", busJ, busI);
        }

        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(ckt);
    }

    private static void addBusesMap(Map<String, List<String>> busesMap, int busI, int busJ, int busK, String ckt) {
        String busString;
        List<Integer> buses = new ArrayList<>();
        buses.add(busI);
        buses.add(busJ);
        buses.add(busK);
        Collections.sort(buses);
        busString = String.format("%06d-%06d-%06d", buses.get(0), buses.get(1), buses.get(2));

        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(ckt);
    }

    private static int firstBus(String busKey) {
        String[] tokens = busKey.split("-");
        return Integer.parseInt(tokens[0]);
    }

    private static int secondBus(String busKey) {
        String[] tokens = busKey.split("-");
        return Integer.parseInt(tokens[1]);
    }

    private static int thirdBus(String busKey) {
        String[] tokens = busKey.split("-");
        return Integer.parseInt(tokens[2]);
    }

    private void checkDuplicates(String tag, String tagEquipments, Map<String, List<String>> duplicatedBusesEquipments) {
        if (!duplicatedBusesEquipments.isEmpty()) {
            duplicatedBusesEquipments.forEach((key, value) -> warnings
                .add(String.format("%s: Multiple %s (%d) at bus %d with the same Id %s", tag, tagEquipments,
                    value.size(), Integer.valueOf(key), value.get(0))));
            validCase = false;
        }
    }

    private void checkDuplicatesLinks(String tag, String tagLinks, Map<String, List<String>> duplicatedBusesLinks) {
        if (!duplicatedBusesLinks.isEmpty()) {
            duplicatedBusesLinks.forEach((key, value) -> warnings
                .add(String.format("%s: Multiple %s (%d) between buses %d and %d with the same Id %s", tag, tagLinks,
                    value.size(), firstBus(key), secondBus(key), value.get(0))));
            validCase = false;
        }
    }

    private String getWarningTransformer1Parameter(String id, String tag, double param) {
        return String.format(Locale.US, WARNING_TRANSFORMER_1_PARAMETER, id, tag, param);
    }

    private String getWarningTransformer2Parameters(String id, String tag, double param1, double param2) {
        return String.format(Locale.US, WARNING_TRANSFORMER_2_PARAMETERS, id, tag, param1, param2);
    }
}
