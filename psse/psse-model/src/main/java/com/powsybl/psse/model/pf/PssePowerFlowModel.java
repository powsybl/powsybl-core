/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PssePowerFlowModel {

    private final PsseCaseIdentification caseIdentification;

    private final List<PsseBus> buses = new ArrayList<>();

    private final List<PsseLoad> loads = new ArrayList<>();

    private final List<PsseFixedShunt> fixedShunts = new ArrayList<>();

    private final List<PsseGenerator> generators = new ArrayList<>();

    private final List<PsseNonTransformerBranch> nonTransformerBranches = new ArrayList<>();

    private final List<PsseTransformer> transformers = new ArrayList<>();

    private final List<PsseArea> areas = new ArrayList<>();

    private final List<PsseTwoTerminalDcTransmissionLine> twoTerminalDcTransmissionLines = new ArrayList<>();

    private final List<PsseVoltageSourceConverterDcTransmissionLine> voltageSourceConverterDcTransmissionLines = new ArrayList<>();

    private final List<PsseTransformerImpedanceCorrection> transformerImpedanceCorrections = new ArrayList<>();

    private final List<PsseMultiTerminalDcTransmissionLine> multiTerminalDcTransmissionLines = new ArrayList<>();

    private final List<PsseLineGrouping> lineGrouping = new ArrayList<>();

    private final List<PsseZone> zones = new ArrayList<>();

    private final List<PsseInterareaTransfer> interareaTransfer = new ArrayList<>();

    private final List<PsseOwner> owners = new ArrayList<>();

    private final List<PsseFacts> facts = new ArrayList<>();

    private final List<PsseSwitchedShunt> switchedShunts = new ArrayList<>();

    private final List<PsseGneDevice> gneDevice = new ArrayList<>();

    private final List<PsseInductionMachine> inductionMachines = new ArrayList<>();

    private final List<PsseSubstation> substations = new ArrayList<>();

    public PssePowerFlowModel(PsseCaseIdentification caseIdentification) {
        this.caseIdentification = Objects.requireNonNull(caseIdentification);
    }

    public PsseCaseIdentification getCaseIdentification() {
        return caseIdentification;
    }

    public void addBuses(List<PsseBus> buses) {
        this.buses.addAll(modelled(buses));
    }

    public void replaceAllBuses(List<PsseBus> buses) {
        this.buses.clear();
        this.buses.addAll(modelled(buses));
    }

    public List<PsseBus> getBuses() {
        return Collections.unmodifiableList(buses);
    }

    public void addLoads(List<PsseLoad> loads) {
        this.loads.addAll(modelled(loads));
    }

    public void replaceAllLoads(List<PsseLoad> loads) {
        this.loads.clear();
        this.loads.addAll(modelled(loads));
    }

    public List<PsseLoad> getLoads() {
        return Collections.unmodifiableList(loads);
    }

    public void addFixedShunts(List<PsseFixedShunt> fixedShunts) {
        this.fixedShunts.addAll(fixedShunts);
    }

    public void replaceAllFixedShunts(List<PsseFixedShunt> fixedShunts) {
        this.fixedShunts.clear();
        this.fixedShunts.addAll(fixedShunts);
    }

    public List<PsseFixedShunt> getFixedShunts() {
        return Collections.unmodifiableList(fixedShunts);
    }

    public void addGenerators(List<PsseGenerator> generators) {
        this.generators.addAll(modelled(generators));
    }

    public void replaceAllGenerators(List<PsseGenerator> generators) {
        this.generators.clear();
        this.generators.addAll(modelled(generators));
    }

    public List<PsseGenerator> getGenerators() {
        return Collections.unmodifiableList(generators);
    }

    public void addNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches) {
        this.nonTransformerBranches.addAll(modelled(nonTransformerBranches));
    }

    public void replaceAllNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches) {
        this.nonTransformerBranches.clear();
        this.nonTransformerBranches.addAll(modelled(nonTransformerBranches));
    }

    public List<PsseNonTransformerBranch> getNonTransformerBranches() {
        return Collections.unmodifiableList(nonTransformerBranches);
    }

    public void addTransformers(List<PsseTransformer> transformers) {
        this.transformers.addAll(modelled(transformers));
    }

    public void replaceAllTransformers(List<PsseTransformer> transformers) {
        this.transformers.clear();
        this.transformers.addAll(modelled(transformers));
    }

    public List<PsseTransformer> getTransformers() {
        return Collections.unmodifiableList(transformers);
    }

    public void addAreas(List<PsseArea> areas) {
        this.areas.addAll(areas);
    }

    public List<PsseArea> getAreas() {
        return Collections.unmodifiableList(areas);
    }

    public void addTwoTerminalDcTransmissionLines(List<PsseTwoTerminalDcTransmissionLine> twoTerminalDcTransmissionLines) {
        this.twoTerminalDcTransmissionLines.addAll(modelled(twoTerminalDcTransmissionLines));
    }

    public void replaceAllTwoTerminalDcTransmissionLines(List<PsseTwoTerminalDcTransmissionLine> twoTerminalDcTransmissionLines) {
        this.twoTerminalDcTransmissionLines.clear();
        this.twoTerminalDcTransmissionLines.addAll(twoTerminalDcTransmissionLines);
    }

    public List<PsseTwoTerminalDcTransmissionLine> getTwoTerminalDcTransmissionLines() {
        return Collections.unmodifiableList(twoTerminalDcTransmissionLines);
    }

    public void addVoltageSourceConverterDcTransmissionLines(List<PsseVoltageSourceConverterDcTransmissionLine> voltageSourceConverterDcTransmissionLines) {
        this.voltageSourceConverterDcTransmissionLines.addAll(modelled(voltageSourceConverterDcTransmissionLines));
    }

    public List<PsseVoltageSourceConverterDcTransmissionLine> getVoltageSourceConverterDcTransmissionLines() {
        return Collections.unmodifiableList(voltageSourceConverterDcTransmissionLines);
    }

    public void addTransformerImpedanceCorrections(List<PsseTransformerImpedanceCorrection> transformerImpedanceCorrections) {
        this.transformerImpedanceCorrections.addAll(transformerImpedanceCorrections);
    }

    public List<PsseTransformerImpedanceCorrection> getTransformerImpedanceCorrections() {
        return Collections.unmodifiableList(transformerImpedanceCorrections);
    }

    public void addMultiTerminalDcTransmissionLines(List<PsseMultiTerminalDcTransmissionLine> multiTerminalDcTransmissionLines) {
        this.multiTerminalDcTransmissionLines.addAll(multiTerminalDcTransmissionLines);
    }

    public List<PsseMultiTerminalDcTransmissionLine> getMultiTerminalDcTransmissionLines() {
        return Collections.unmodifiableList(multiTerminalDcTransmissionLines);
    }

    public void addLineGrouping(List<PsseLineGrouping> lineGrouping) {
        this.lineGrouping.addAll(lineGrouping);
    }

    public List<PsseLineGrouping> getLineGrouping() {
        return Collections.unmodifiableList(lineGrouping);
    }

    public void addZones(List<PsseZone> zones) {
        this.zones.addAll(zones);
    }

    public List<PsseZone> getZones() {
        return Collections.unmodifiableList(zones);
    }

    public void addInterareaTransfer(List<PsseInterareaTransfer> interareaTransfer) {
        this.interareaTransfer.addAll(interareaTransfer);
    }

    public List<PsseInterareaTransfer> getInterareaTransfer() {
        return Collections.unmodifiableList(interareaTransfer);
    }

    public void addOwners(List<PsseOwner> owners) {
        this.owners.addAll(owners);
    }

    public List<PsseOwner> getOwners() {
        return Collections.unmodifiableList(owners);
    }

    public void addFacts(List<PsseFacts> facts) {
        this.facts.addAll(modelled(facts));
    }

    public List<PsseFacts> getFacts() {
        return Collections.unmodifiableList(facts);
    }

    public void addSwitchedShunts(List<PsseSwitchedShunt> switchedShunts) {
        this.switchedShunts.addAll(modelled(switchedShunts));
    }

    public void replaceAllSwitchedShunts(List<PsseSwitchedShunt> switchedShunts) {
        this.switchedShunts.clear();
        this.switchedShunts.addAll(switchedShunts);
    }

    public List<PsseSwitchedShunt> getSwitchedShunts() {
        return Collections.unmodifiableList(switchedShunts);
    }

    public void addGneDevice(List<PsseGneDevice> gneDevice) {
        this.gneDevice.addAll(gneDevice);
    }

    public List<PsseGneDevice> getGneDevice() {
        return Collections.unmodifiableList(gneDevice);
    }

    public void addInductionMachines(List<PsseInductionMachine> inductionMachines) {
        this.inductionMachines.addAll(inductionMachines);
    }

    public List<PsseInductionMachine> getInductionMachines() {
        return Collections.unmodifiableList(inductionMachines);
    }

    public void addSubstations(List<PsseSubstation> substations) {
        this.substations.addAll(substations);
    }

    public List<PsseSubstation> getSubstations() {
        return Collections.unmodifiableList(substations);
    }

    public PssePowerFlowModel referenceAndCopyPssePowerFlowModel() {
        PssePowerFlowModel newPsseModel = new PssePowerFlowModel(this.getCaseIdentification());
        referencePermanentBlocks(this, newPsseModel);
        copyModifiedBlocks(this, newPsseModel);
        return newPsseModel;
    }

    private static void referencePermanentBlocks(PssePowerFlowModel psseModel, PssePowerFlowModel newPsseModel) {
        newPsseModel.addAreas(psseModel.getAreas());
        newPsseModel.addTwoTerminalDcTransmissionLines(psseModel.getTwoTerminalDcTransmissionLines());
        newPsseModel.addVoltageSourceConverterDcTransmissionLines(psseModel.getVoltageSourceConverterDcTransmissionLines());
        newPsseModel.addTransformerImpedanceCorrections(psseModel.getTransformerImpedanceCorrections());
        newPsseModel.addMultiTerminalDcTransmissionLines(psseModel.getMultiTerminalDcTransmissionLines());
        newPsseModel.addLineGrouping(psseModel.getLineGrouping());
        newPsseModel.addZones(psseModel.getZones());
        newPsseModel.addInterareaTransfer(psseModel.getInterareaTransfer());
        newPsseModel.addOwners(psseModel.getOwners());
        newPsseModel.addFacts(psseModel.getFacts());
        newPsseModel.addGneDevice(psseModel.getGneDevice());
        newPsseModel.addInductionMachines(psseModel.getInductionMachines());
    }

    private static void copyModifiedBlocks(PssePowerFlowModel psseModel, PssePowerFlowModel newPsseModel) {
        psseModel.getSubstations().forEach(psseSubstation -> newPsseModel.substations.add(psseSubstation.copy()));
        psseModel.getBuses().forEach(psseBus -> newPsseModel.buses.add(psseBus.copy()));
        psseModel.getLoads().forEach(psseLoad -> newPsseModel.loads.add(psseLoad.copy()));

        psseModel.getFixedShunts().forEach(psseFixedShunt -> newPsseModel.fixedShunts.add(psseFixedShunt.copy()));
        psseModel.getGenerators().forEach(psseGenerator -> newPsseModel.generators.add(psseGenerator.copy()));
        psseModel.getNonTransformerBranches().forEach(nonTransformerBranch -> newPsseModel.nonTransformerBranches.add(nonTransformerBranch.copy()));
        psseModel.getTransformers().forEach(psseTransformer -> newPsseModel.transformers.add(psseTransformer.copy()));
        psseModel.getSwitchedShunts().forEach(psseSwitchedShunt -> newPsseModel.switchedShunts.add(psseSwitchedShunt.copy()));
    }

    private <T extends PsseVersioned> List<T> modelled(List<T> elements) {
        for (PsseVersioned v : elements) {
            v.setModel(this);
        }
        return elements;
    }
}
