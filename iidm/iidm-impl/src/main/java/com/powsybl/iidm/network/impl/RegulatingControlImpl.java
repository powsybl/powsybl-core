/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.regulation.EquipmentSide;
import com.powsybl.iidm.network.regulation.RegulatingControl;
import com.powsybl.iidm.network.regulation.Regulation;
import com.powsybl.iidm.network.regulation.RegulationKind;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulatingControlImpl implements RegulatingControl, MultiVariantObject, Validable {

    private final NetworkImpl network;
    private final String id;
    private final RegulationKind regulationKind;
    private final String regulatedEquipmentId;
    private final EquipmentSide regulatedEquipmentSide;
    private final List<Regulation> regulations;
    private final RegulatingControlListImpl parent;

    // attributes depending on the variant
    private final TDoubleArrayList targetValue;
    private final TDoubleArrayList targetDeadband;

    RegulatingControlImpl(NetworkImpl network, String id, double targetValue, double targetDeadband, RegulationKind regulationKind, String regulatedEquipmentId, EquipmentSide regulatedEquipmentSide, RegulatingControlListImpl parent) {
        this.network = network;
        this.id = Objects.requireNonNull(id);

        int variantArraySize = network.getVariantManager().getVariantArraySize();
        this.targetValue = new TDoubleArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetValue.add(targetValue);
            this.targetDeadband.add(targetDeadband);
        }

        this.regulationKind = Objects.requireNonNull(regulationKind);
        this.regulatedEquipmentId = Objects.requireNonNull(regulatedEquipmentId);
        this.regulatedEquipmentSide = regulatedEquipmentSide;
        this.regulations = new ArrayList<>();

        this.parent = Objects.requireNonNull(parent);
        this.parent.addRegulatingControl(this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getTargetValue() {
        return targetValue.get(network.getVariantIndex());
    }

    @Override
    public RegulatingControl setTargetValue(double targetValue) {
        if (Double.isNaN(targetValue)) {
            throw new ValidationException(this, "Undefined target value for regulating control " + id);
        }
        int variantIndex = network.getVariantIndex();
        double oldValue = this.targetValue.get(variantIndex);
        this.targetValue.set(variantIndex, targetValue);
        network.getListeners().notifyUpdate(network.getIdentifiable(regulatedEquipmentId), "targetValue" + (regulatedEquipmentSide != null ? regulatedEquipmentSide.index() : ""), oldValue, targetValue);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(network.getVariantIndex());
    }

    @Override
    public RegulatingControl setTargetDeadband(double targetDeadband) {
        int variantIndex = network.getVariantIndex();
        double oldValue = this.targetDeadband.get(variantIndex);
        this.targetDeadband.set(variantIndex, targetDeadband);
        network.getListeners().notifyUpdate(network.getIdentifiable(regulatedEquipmentId), "targetDeadband" + (regulatedEquipmentSide != null ? regulatedEquipmentSide.index() : ""), oldValue, targetDeadband);
        return this;
    }

    @Override
    public RegulationKind getRegulationKind() {
        return regulationKind;
    }

    @Override
    public String getRegulatedEquipmentId() {
        return regulatedEquipmentId;
    }

    @Override
    public EquipmentSide getRegulatedEquipmentSide() {
        return regulatedEquipmentSide;
    }

    @Override
    public List<Regulation> getRegulations() {
        return Collections.unmodifiableList(regulations);
    }

    @Override
    public void remove() {
        this.parent.removeRegulatingControl(id);
    }

    NetworkImpl getNetwork() {
        return network;
    }

    void addRegulation(RegulationImpl regulation) {
        regulations.add(regulation);
    }

    void removeRegulation(RegulationImpl regulation) {
        regulations.remove(regulation);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        targetValue.ensureCapacity(targetValue.size() + number);
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        for (int i = 0; i < number; i++) {
            targetValue.add(targetValue.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        targetValue.remove(targetValue.size() - number, number);
        targetDeadband.remove(targetDeadband.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            targetValue.set(index, targetValue.get(sourceIndex));
            targetDeadband.set(index, targetDeadband.get(sourceIndex));
        }
    }

    @Override
    public String getMessageHeader() {
        return "Regulating control '" + id + "': ";
    }
}
