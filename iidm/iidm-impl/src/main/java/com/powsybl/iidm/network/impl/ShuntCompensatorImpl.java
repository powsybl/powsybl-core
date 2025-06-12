/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ShuntCompensatorImpl extends AbstractConnectable<ShuntCompensator> implements ShuntCompensator {

    private static final String SHUNT_COMPENSATOR = "shunt compensator";

    private final Ref<? extends VariantManagerHolder> network;

    private final ShuntCompensatorModelExt model;

    /* the regulating terminal */
    private final RegulatingPoint regulatingPoint;

    // attributes depending on the variant

    /* the current number of section switched on */
    private final ArrayList<Integer> sectionCount;

    /* the solved number of section switched on */
    private final ArrayList<Integer> solvedSectionCount;

    /* the target voltage value */
    private final TDoubleArrayList targetV;

    /* the target deadband */
    private final TDoubleArrayList targetDeadband;

    ShuntCompensatorImpl(Ref<NetworkImpl> network,
                         String id, String name, boolean fictitious, ShuntCompensatorModelExt model,
                         Integer sectionCount, Integer solvedSectionCount, TerminalExt regulatingTerminal,
                         Boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        super(network, id, name, fictitious);
        this.network = network;
        int variantArraySize = this.network.get().getVariantManager().getVariantArraySize();
        regulatingPoint = new RegulatingPoint(id, this::getTerminal, variantArraySize, voltageRegulatorOn, true);
        regulatingPoint.setRegulatingTerminal(regulatingTerminal);
        this.sectionCount = new ArrayList<>(variantArraySize);
        this.solvedSectionCount = new ArrayList<>(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.sectionCount.add(sectionCount);
            this.solvedSectionCount.add(checkSolvedSectionCount(solvedSectionCount, model.getMaximumSectionCount()));
            this.targetV.add(targetV);
            this.targetDeadband.add(targetDeadband);
        }
        this.model = Objects.requireNonNull(model).attach(this);
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public int getSectionCount() {
        Integer section = sectionCount.get(network.get().getVariantIndex());
        if (section == null) {
            throw ValidationUtil.createUndefinedValueGetterException();
        }
        return section;
    }

    @Override
    public Integer getSolvedSectionCount() {
        return solvedSectionCount.get(network.get().getVariantIndex());
    }

    @Override
    public OptionalInt findSectionCount() {
        Integer section = sectionCount.get(network.get().getVariantIndex());
        return section == null ? OptionalInt.empty() : OptionalInt.of(section);
    }

    @Override
    public int getMaximumSectionCount() {
        return model.getMaximumSectionCount();
    }

    @Override
    public ShuntCompensatorImpl setSectionCount(int sectionCount) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkSections(this, sectionCount, model.getMaximumSectionCount(), getNetwork().getMinValidationLevel(),
                getNetwork().getReportNodeContext().getReportNode());
        if (sectionCount < 0 || sectionCount > model.getMaximumSectionCount()) {
            throw new ValidationException(this, "unexpected section number (" + sectionCount + "): no existing associated section");
        }
        int variantIndex = n.getVariantIndex();
        Integer oldValue = this.sectionCount.set(variantIndex, sectionCount);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("sectionCount", variantId, oldValue, sectionCount);
        return this;
    }

    @Override
    public ShuntCompensator unsetSectionCount() {
        NetworkImpl n = getNetwork();
        ValidationUtil.throwExceptionOrIgnore(this, "count of sections in service has been unset", n.getMinValidationLevel());
        int variantIndex = network.get().getVariantIndex();
        Integer oldValue = this.sectionCount.set(variantIndex, null);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("sectionCount", variantId, oldValue, null);
        return this;
    }

    @Override
    public ShuntCompensatorImpl setSolvedSectionCount(int solvedSectionCount) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        Integer oldValue = this.solvedSectionCount.set(variantIndex, checkSolvedSectionCount(solvedSectionCount, this.model.getMaximumSectionCount()));
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("solvedSectionCount", variantId, oldValue, solvedSectionCount);
        return this;
    }

    @Override
    public ShuntCompensator unsetSolvedSectionCount() {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        Integer oldValue = this.solvedSectionCount.set(variantIndex, null);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("solvedSectionCount", variantId, oldValue, null);
        return this;
    }

    @Override
    public double getB() {
        return model.getB(sectionCount.get(network.get().getVariantIndex()));
    }

    @Override
    public double getG() {
        return model.getG(sectionCount.get(network.get().getVariantIndex()));
    }

    @Override
    public double getB(int sectionCount) {
        return model.getB(sectionCount);
    }

    @Override
    public double getG(int sectionCount) {
        return model.getG(sectionCount);
    }

    @Override
    public ShuntCompensatorModelType getModelType() {
        return model.getType();
    }

    @Override
    public ShuntCompensatorModel getModel() {
        return model;
    }

    @Override
    public <M extends ShuntCompensatorModel> M getModel(Class<M> modelType) {
        if (modelType == null) {
            throw new IllegalArgumentException("shunt compensator model type is null");
        }
        if (modelType.isInstance(model)) {
            return modelType.cast(model);
        }
        throw new ValidationException(this, "incorrect shunt compensator model type " +
                modelType.getName() + ", expected " + model.getClass());
    }

    @Override
    public TerminalExt getRegulatingTerminal() {
        return regulatingPoint.getRegulatingTerminal();
    }

    @Override
    public ShuntCompensatorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        Terminal oldValue = regulatingPoint.getRegulatingTerminal();
        regulatingPoint.setRegulatingTerminal((TerminalExt) regulatingTerminal);
        notifyUpdate("regulatingTerminal", oldValue, regulatingPoint.getRegulatingTerminal());
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return regulatingPoint.isRegulating(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV.get(variantIndex),
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        ValidationUtil.checkTargetDeadband(this, SHUNT_COMPENSATOR, voltageRegulatorOn, targetDeadband.get(variantIndex),
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        boolean oldValue = regulatingPoint.setRegulating(variantIndex, voltageRegulatorOn);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public double getTargetV() {
        return targetV.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, regulatingPoint.isRegulating(variantIndex), targetV,
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        double oldValue = this.targetV.set(variantIndex, targetV);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetV", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setTargetDeadband(double targetDeadband) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(this, SHUNT_COMPENSATOR, regulatingPoint.isRegulating(variantIndex), targetDeadband,
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetDeadband", variantId, oldValue, targetDeadband);
        return this;
    }

    @Override
    public void remove() {
        regulatingPoint.remove();
        super.remove();
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        sectionCount.ensureCapacity(sectionCount.size() + number);
        solvedSectionCount.ensureCapacity(solvedSectionCount.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        for (int i = 0; i < number; i++) {
            sectionCount.add(sectionCount.get(sourceIndex));
            solvedSectionCount.add(solvedSectionCount.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
        }
        regulatingPoint.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        List<Integer> tmpInt = new ArrayList<>(sectionCount.subList(0, sectionCount.size() - number));
        sectionCount.clear();
        sectionCount.addAll(tmpInt);
        List<Integer> solvedSectionCountTmp = new ArrayList<>(solvedSectionCount.subList(0, solvedSectionCount.size() - number));
        solvedSectionCount.clear();
        solvedSectionCount.addAll(solvedSectionCountTmp);
        targetV.remove(targetV.size() - number, number);
        targetDeadband.remove(targetDeadband.size() - number, number);
        regulatingPoint.reduceVariantArraySize(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        regulatingPoint.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            sectionCount.set(index, sectionCount.get(sourceIndex));
            solvedSectionCount.set(index, solvedSectionCount.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
            targetDeadband.set(index, targetDeadband.get(sourceIndex));
        }
        regulatingPoint.allocateVariantArrayElement(indexes, sourceIndex);
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

    private Integer checkSolvedSectionCount(Integer solvedSectionCount, int maximumSectionCount) {
        if (solvedSectionCount != null && (solvedSectionCount < 0 || solvedSectionCount > maximumSectionCount)) {
            throw new ValidationException(this, "unexpected solved section number (" + solvedSectionCount + "): no existing associated section");
        }
        return solvedSectionCount;
    }
}
