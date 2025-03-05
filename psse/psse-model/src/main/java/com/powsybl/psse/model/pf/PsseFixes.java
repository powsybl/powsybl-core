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

import static com.powsybl.psse.model.pf.PsseValidation.switchedShuntId;
import static com.powsybl.psse.model.pf.PsseValidation.switchedShuntRegulatingBus;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseFixes {

    private static final String WINDING_1 = "Winding1";

    private static final Logger LOGGER = LoggerFactory.getLogger(PsseFixes.class);
    private final PssePowerFlowModel model;
    private final PsseVersion version;

    public PsseFixes(PssePowerFlowModel model, PsseVersion version) {
        this.model = Objects.requireNonNull(model);
        this.version = Objects.requireNonNull(version);
    }

    public void fix() {
        fixDuplicatedIds();
        fixTransformersWindingCod();
        fixControlledBuses();
    }

    private void fixDuplicatedIds() {
        fixDuplicatedIds(model.getLoads(), l -> l.getI() + "-" + l.getId(), this::loadFixer);
        fixDuplicatedIds(model.getGenerators(), g -> g.getI() + "-" + g.getId(), this::generatorFixer);
        fixDuplicatedIds(model.getFixedShunts(), sh -> sh.getI() + "-" + sh.getId(), this::fixedShuntFixer);
        // For branches and transformers, we build the complete id using the sorted nodes plus the circuit id
        fixDuplicatedIds(model.getNonTransformerBranches(), b -> sortedBuses(b.getI(), b.getJ()) + b.getCkt(), this::nonTransformerBranchFixer);
        fixDuplicatedIds(model.getTransformers(), b -> sortedBuses(b.getI(), b.getJ(), b.getK()) + b.getCkt(), this::transformerFixer);
        if (version.getMajorNumber() >= 35) {
            fixDuplicatedIds(model.getSwitchedShunts(), sh -> sh.getI() + "-" + sh.getId(), this::switchedShuntFixer);
        }
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
        fixSwitchedShuntControlledBus(buses);
    }

    private String sortedBuses(int... buses) {
        Arrays.sort(buses);
        return Arrays.toString(buses);
    }

    private <T> void fixDuplicatedIds(List<T> psseObjects, Function<T, String> idBuilder, IdFixer<T> idFixer) {
        Set<String> foundIds = new HashSet<>();
        Map<String, List<T>> duplicated = new HashMap<>();
        psseObjects.forEach(psseObject -> {
            String id = idBuilder.apply(psseObject);
            if (foundIds.contains(id)) {
                duplicated.computeIfAbsent(id, key -> new ArrayList<>()).add(psseObject);
            }
            foundIds.add(id);
        });
        duplicated.forEach((id, duplicatedIdObjects) -> {
            final Set<String> usedIds = new HashSet<>();
            usedIds.add(id);
            duplicatedIdObjects.forEach(psseObject -> {
                String fixedId = idFixer.fix(psseObject, usedIds);
                usedIds.add(fixedId);
            });
        });
    }

    private String buildFixedId2(String id, Set<String> usedIds) {
        // The id is only two characters long, and we try to preserve the first character
        // We avoid setting a 0 as first character because PSSE may interpret them as not valid
        String first = id.isEmpty() ? "1" : id.substring(0, 1);
        for (char second = '0'; second <= '9'; second++) {
            String candidate = first + second;
            if (usedIds.contains(candidate)) {
                continue;
            }
            return candidate;
        }
        // try with an uppercase letter, if all digits have been used
        for (char second = 'A'; second <= 'Z'; second++) {
            String candidate = first + second;
            if (usedIds.contains(candidate)) {
                continue;
            }
            return candidate;
        }
        // we run out of candidates, return the same id
        return id;
    }

    private String loadFixer(PsseLoad load, Set<String> usedIds) {
        String id = load.getId();
        String fixedId = buildFixedId2(id, usedIds);
        load.setId(fixedId);
        warn("Load", load.getI(), id, fixedId);
        return fixedId;
    }

    private String generatorFixer(PsseGenerator generator, Set<String> usedIds) {
        String id = generator.getId();
        String fixedId = buildFixedId2(id, usedIds);
        generator.setId(fixedId);
        warn("Generator", generator.getI(), id, fixedId);
        return fixedId;
    }

    private String fixedShuntFixer(PsseFixedShunt fixedShunt, Set<String> usedIds) {
        String id = fixedShunt.getId();
        String fixedId = buildFixedId2(id, usedIds);
        fixedShunt.setId(fixedId);
        warn("FixedShunt", fixedShunt.getI(), id, fixedId);
        return fixedId;
    }

    private String switchedShuntFixer(PsseSwitchedShunt switchedShunt, Set<String> usedIds) {
        String id = switchedShunt.getId();
        String fixedId = buildFixedId2(id, usedIds);
        switchedShunt.setId(fixedId);
        warn("SwitchedShunt", switchedShunt.getI(), id, fixedId);
        return fixedId;
    }

    private String nonTransformerBranchFixer(PsseNonTransformerBranch branch, Set<String> usedIds) {
        String id = branch.getCkt();
        String fixedId = buildFixedId2(id, usedIds);
        branch.setCkt(fixedId);
        warn(branch, id, fixedId);
        return fixedId;
    }

    private String transformerFixer(PsseTransformer transformer, Set<String> usedIds) {
        String id = transformer.getCkt();
        String fixedId = buildFixedId2(id, usedIds);
        transformer.setCkt(fixedId);
        warn(transformer, id, fixedId);
        return fixedId;
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

    private void fixSwitchedShuntControlledBus(Set<Integer> buses) {
        model.getSwitchedShunts().forEach(psseSwitchedShunt -> {
            if (switchedShuntRegulatingBus(psseSwitchedShunt, version) != 0 && !buses.contains(switchedShuntRegulatingBus(psseSwitchedShunt, version))) {
                warn("SwitchedShunt", String.format("%s, ...", switchedShuntId(psseSwitchedShunt, version)), switchedShuntRegulatingBus(psseSwitchedShunt, version));
                psseSwitchedShunt.setSwreg(0);
                psseSwitchedShunt.setSwrem(0);
            }
        });
    }

    private void warn(String type, int i, String id, String fixedId) {
        if (LOGGER.isWarnEnabled()) {
            if (id.equals(fixedId)) {
                LOGGER.warn("Unable to fix {} Id: I {} ID '{}'", type, i, id);
            } else {
                LOGGER.warn("{} Id fixed: I {} ID '{}'. Fixed ID '{}'", type, i, id, fixedId);
            }
        }
    }

    private void warn(PsseNonTransformerBranch branch, String id, String fixedId) {
        if (LOGGER.isWarnEnabled()) {
            if (id.equals(fixedId)) {
                LOGGER.warn("Unable to fix NonTransformerBranch Id: I {} J {} CKT '{}'", branch.getI(), branch.getJ(), id);
            } else {
                LOGGER.warn("NonTransformerBranch Id fixed: I {} J {} CKT '{}'. Fixed CKT '{}'", branch.getI(), branch.getJ(), id, fixedId);
            }
        }
    }

    private void warn(PsseTransformer transformer, String id, String fixedId) {
        if (LOGGER.isWarnEnabled()) {
            if (id.equals(fixedId)) {
                LOGGER.warn("Unable to fix Transformer Id: I {} J {} K {} CKT '{}'", transformer.getI(), transformer.getJ(), transformer.getK(), id);
            } else {
                LOGGER.warn("Transformer Id fixed: I {} J {} K {} CKT '{}'. Fixed CKT '{}'", transformer.getI(), transformer.getJ(), transformer.getK(), id, fixedId);
            }
        }
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
}
