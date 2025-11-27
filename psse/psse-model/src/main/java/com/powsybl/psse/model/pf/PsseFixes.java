/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

import static com.powsybl.psse.model.pf.PsseValidation.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseFixes {

    private static final String WINDING_1 = "Winding1";

    private static final Logger LOGGER = LoggerFactory.getLogger(PsseFixes.class);
    private final PssePowerFlowModel model;
    private final PsseVersion version;
    private static final double NON_TRANSFORMER_BRANCH_X = 1e-4;

    public PsseFixes(PssePowerFlowModel model, PsseVersion version) {
        this.model = Objects.requireNonNull(model);
        this.version = Objects.requireNonNull(version);
    }

    public void fix() {
        fixDuplicatedIds();
        // fix the controlled buses before fixing the winding cod
        fixControlledBuses();
        fixTransformersWindingCod();
        fixNonTransformersBranchX();
    }

    private void fixDuplicatedIds() {
        model.replaceAllBuses(fixDuplicatedIds(model.getBuses(), b -> String.valueOf(b.getI()), "Bus"));
        model.replaceAllLoads(fixDuplicatedIds(model.getLoads(), l -> l.getI() + "-" + l.getId(), "Load"));
        model.replaceAllGenerators(fixDuplicatedIds(model.getGenerators(), g -> g.getI() + "-" + g.getId(), "Generator"));
        model.replaceAllFixedShunts(fixDuplicatedIds(model.getFixedShunts(), sh -> sh.getI() + "-" + sh.getId(), "FixedShunt"));
        // For branches and transformers, we build the complete id using the sorted nodes plus the circuit id
        model.replaceAllNonTransformerBranches(fixDuplicatedIds(model.getNonTransformerBranches(), b -> sortedBuses(b.getI(), b.getJ()) + b.getCkt(), "NonTransformerBranch"));
        model.replaceAllTransformers(fixDuplicatedIds(model.getTransformers(), b -> sortedBuses(b.getI(), b.getJ(), b.getK()) + b.getCkt(), "Transformer"));
        if (version.getMajorNumber() >= 35) {
            model.replaceAllSwitchedShunts(fixDuplicatedIds(model.getSwitchedShunts(), sh -> sh.getI() + "-" + sh.getId(), "SwitchedShunt"));
        }
        model.replaceAllTwoTerminalDcTransmissionLines(fixDuplicatedIds(model.getTwoTerminalDcTransmissionLines(), PsseTwoTerminalDcTransmissionLine::getName, "TwoTerminalDcLine"));
        model.replaceAllVoltageSourceConverterDcTransmissionLines(fixDuplicatedIds(model.getVoltageSourceConverterDcTransmissionLines(), PsseVoltageSourceConverterDcTransmissionLine::getName, "VscDcLine"));
        model.replaceAllFacts(fixDuplicatedIds(model.getFacts(), PsseFacts::getName, "Facts"));
        model.replaceAllAreas(fixDuplicatedIds(model.getAreas(), a -> String.valueOf(a.getI()), "Area"));
    }

    private void fixTransformersWindingCod() {
        model.getTransformers().forEach(psseTransformer -> {
            if (psseTransformer.getK() == 0) { // TwoWindingsTransformers
                fixTransformerWindingCod(psseTransformer, psseTransformer.getWinding1(), WINDING_1);
            } else {
                fixTransformerWindingCod(psseTransformer, psseTransformer.getWinding1(), WINDING_1);
                fixTransformerWindingCod(psseTransformer, psseTransformer.getWinding2(), "Winding2");
                fixTransformerWindingCod(psseTransformer, psseTransformer.getWinding3(), "Winding3");
            }
        });
    }

    private void fixControlledBuses() {
        Set<Integer> buses = new HashSet<>();
        model.getBuses().forEach(psseBus -> buses.add(psseBus.getI()));
        fixGeneratorsControlledBus(buses);
        fixTransformersControlledBus(buses);
        fixVscDcTransmissionLineControlledBus(buses);
        fixFactsDeviceControlledBus(buses);
        fixSwitchedShuntControlledBus(buses);
    }

    private String sortedBuses(int... buses) {
        Arrays.sort(buses);
        return Arrays.toString(buses);
    }

    private <T> List<T> fixDuplicatedIds(List<T> psseObjects, Function<T, String> idBuilder, String elementTypeName) {
        List<T> unique = new ArrayList<>();
        Map<String, Integer> indexes = new HashMap<>();

        psseObjects.forEach(psseObject -> {
            var id = idBuilder.apply(psseObject);
            if (indexes.containsKey(id)) {
                LOGGER.warn("Duplicated {} Id: {}", elementTypeName, id);
                unique.set(indexes.get(id), psseObject);
            } else {
                indexes.put(id, unique.size());
                unique.add(psseObject);
            }
        });

        return unique;
    }

    private void fixTransformerWindingCod(PsseTransformer transformer, PsseTransformerWinding winding, String windingTag) {
        if (Math.abs(winding.getCod()) == 1 && winding.getCont() == 0) {
            warn(transformer, windingTag, winding.getCod(), 0);
            winding.setCod(0);
        }
    }

    private void fixGeneratorsControlledBus(Set<Integer> buses) {
        model.getGenerators().forEach(psseGenerator -> {
            if (psseGenerator.getIreg() != 0 && !buses.contains(psseGenerator.getIreg())) {
                warn("Generator", String.format("%d, %s, ...", psseGenerator.getI(), psseGenerator.getId()), psseGenerator.getIreg());
                psseGenerator.setIreg(0);
            }
        });
    }

    private void fixTransformersControlledBus(Set<Integer> buses) {
        model.getTransformers().forEach(psseTransformer -> {
            if (psseTransformer.getK() == 0) { // TwoWindingsTransformers
                fixTransformerWindingControlledBus(buses, psseTransformer, psseTransformer.getWinding1(), WINDING_1);
            } else {
                fixTransformerWindingControlledBus(buses, psseTransformer, psseTransformer.getWinding1(), WINDING_1);
                fixTransformerWindingControlledBus(buses, psseTransformer, psseTransformer.getWinding2(), "Winding2");
                fixTransformerWindingControlledBus(buses, psseTransformer, psseTransformer.getWinding3(), "Winding3");
            }
        });
    }

    private void fixTransformerWindingControlledBus(Set<Integer> buses, PsseTransformer psseTransformer, PsseTransformerWinding winding, String windingTag) {
        if (winding.getCont() != 0 && !buses.contains(winding.getCont())) {
            warn("Transformer", String.format("%d, %d, %d, %s, ... %s ", psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt(), windingTag), winding.getCont());
            winding.setCont(0);
        }
    }

    private void fixVscDcTransmissionLineControlledBus(Set<Integer> buses) {
        model.getVoltageSourceConverterDcTransmissionLines().forEach(psseVscDcTransmissionLine -> {
            fixVscDcTransmissionLineConverterControlledBus(buses, psseVscDcTransmissionLine, psseVscDcTransmissionLine.getConverter1(), "Converter1");
            fixVscDcTransmissionLineConverterControlledBus(buses, psseVscDcTransmissionLine, psseVscDcTransmissionLine.getConverter2(), "Converter2");
        });
    }

    private void fixVscDcTransmissionLineConverterControlledBus(Set<Integer> buses, PsseVoltageSourceConverterDcTransmissionLine psseVscDcTransmissionLine, PsseVoltageSourceConverter psseVscDcConverter, String converterTag) {
        if (vscDcTransmissionLineRegulatingBus(psseVscDcConverter, version) != 0 && !buses.contains(vscDcTransmissionLineRegulatingBus(psseVscDcConverter, version))) {
            warn("VoltageSourceConverterDcTransmissionLine", String.format("%s, ... %s", psseVscDcTransmissionLine.getName(), converterTag), vscDcTransmissionLineRegulatingBus(psseVscDcConverter, version));
            psseVscDcConverter.setVsreg(0);
            psseVscDcConverter.setRemot(0);
        }
    }

    private void fixFactsDeviceControlledBus(Set<Integer> buses) {
        model.getFacts().forEach(psseFactsDevice -> {
            if (factsDeviceRegulatingBus(psseFactsDevice, version) != 0 && !buses.contains(factsDeviceRegulatingBus(psseFactsDevice, version))) {
                warn("FactsDevice", String.format("%s, ...", psseFactsDevice.getName()), factsDeviceRegulatingBus(psseFactsDevice, version));
                psseFactsDevice.setFcreg(0);
                psseFactsDevice.setRemot(0);
            }
        });
    }

    private void fixSwitchedShuntControlledBus(Set<Integer> buses) {
        model.getSwitchedShunts().forEach(psseSwitchedShunt -> {
            if (switchedShuntRegulatingBus(psseSwitchedShunt, version) != 0 && !buses.contains(switchedShuntRegulatingBus(psseSwitchedShunt, version))) {
                warn("SwitchedShunt", String.format("%s, ...", switchedShuntId(psseSwitchedShunt, version)), switchedShuntRegulatingBus(psseSwitchedShunt, version));
                psseSwitchedShunt.setSwreg(0);
                psseSwitchedShunt.setSwrem(0);
            }
        });
    }

    private void warn(PsseTransformer transformer, String windingTag, int cod, int fixedCod) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Transformer {} Cod fixed: I {} J {} K {} CKT {}. Cod '{}' Fixed Cod '{}'. Controlled bus is not defined (cont == 0).", windingTag, transformer.getI(), transformer.getJ(), transformer.getK(), transformer.getCkt(), cod, fixedCod);
        }
    }

    private void warn(String type, String recordId, int controlledBus) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("{} {} controlled bus {} not found; fixed to 0", type, recordId, controlledBus);
        }
    }

    @FunctionalInterface
    public interface IdFixer<T> {
        String fix(T t, Set<String> usedIds);
    }

    private void fixNonTransformersBranchX() {
        model.getNonTransformerBranches().forEach(branch -> {
            if (branch.getX() == 0) {
                branch.setX(NON_TRANSFORMER_BRANCH_X);
                LOGGER.warn("NonTransformerBranch X fixed: I {} J {} CKT '{}'. Fixed X '{}'", branch.getI(), branch.getJ(), branch.getCkt(), branch.getX());
            }
        });
    }
}
