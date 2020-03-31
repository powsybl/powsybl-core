/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.regulation.EquipmentSide;
import com.powsybl.iidm.network.regulation.RegulatingControl;
import com.powsybl.iidm.network.regulation.Regulation;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulationImpl implements Regulation, MultiVariantObject {

    private final AbstractConnectable regulatingEquipment;
    private final EquipmentSide regulatingSide;
    private final RegulationListImpl regulationList;

    private RegulatingControlImpl regulatingControl;

    // attribute depending on the variant
    private final TBooleanArrayList regulating;

    RegulationImpl(boolean regulating, RegulatingControlImpl regulatingControl, AbstractConnectable regulatingEquipment, EquipmentSide regulatingSide, RegulationListImpl regulationList) {
        int variantArraySize = regulatingEquipment.getNetwork().getVariantManager().getVariantArraySize();
        this.regulating = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.regulating.add(regulating);
        }
        this.regulatingControl = Objects.requireNonNull(regulatingControl);
        this.regulatingControl.addRegulation(this);
        this.regulatingEquipment = Objects.requireNonNull(regulatingEquipment);
        this.regulatingSide = regulatingSide;
        this.regulationList = Objects.requireNonNull(regulationList);
        this.regulationList.addRegulation(this);
    }

    @Override
    public boolean isRegulating() {
        return regulating.get(regulatingEquipment.getNetwork().getVariantIndex());
    }

    @Override
    public Regulation setRegulating(boolean regulating) {
        int variantIndex = regulatingEquipment.getNetwork().getVariantIndex();
        boolean oldValue = this.regulating.get(variantIndex);
        this.regulating.set(variantIndex, regulating);
        String variantId = regulatingEquipment.getNetwork().getVariantManager().getVariantId(variantIndex);
        regulatingEquipment.notifyUpdate("voltageRegulatorOn", variantId, oldValue, regulating);
        return this;
    }

    @Override
    public RegulatingControl getRegulatingControl() {
        return regulatingControl;
    }

    @Override
    public Regulation setRegulatingControl(RegulatingControl control) {
        if (control == null) {
            throw new ValidationException(regulatingEquipment, "Regulating control is null");
        }
        if (regulatingEquipment.getNetwork() != ((RegulatingControlImpl) control).getNetwork()) {
            throw new ValidationException(regulatingEquipment, "Regulating control " + control.getId() + " is not associated with network " + regulatingEquipment.getNetwork().getId());
        }
        this.regulatingControl = (RegulatingControlImpl) control;
        return this;
    }

    @Override
    public Connectable getRegulatingEquipment() {
        return regulatingEquipment;
    }

    @Override
    public EquipmentSide getRegulatingEquipmentSide() {
        return regulatingSide;
    }

    @Override
    public void remove() {
        this.regulatingControl.removeRegulation(this);
        this.regulationList.removeRegulation(this);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        regulating.ensureCapacity(regulating.size() + number);
        for (int i = 0; i < number; i++) {
            regulating.add(regulating.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        regulating.remove(regulating.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            regulating.set(index, regulating.get(sourceIndex));
        }
    }
}
