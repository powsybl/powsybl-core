/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.DcConverter;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractDcConverter<I extends DcConverter<I>> extends AbstractConnectable<I> implements DcConverter<I>, MultiVariantObject {

    protected final List<DcTerminalImpl> dcTerminals = new ArrayList<>();

    private double idleLoss;
    private double switchingLoss;
    private double resistiveLoss;

    AbstractDcConverter(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                        double idleLoss, double switchingLoss, double resistiveLoss) {
        super(ref, id, name, fictitious);
        this.idleLoss = idleLoss;
        this.switchingLoss = switchingLoss;
        this.resistiveLoss = resistiveLoss;
    }

    @Override
    public Terminal getTerminal1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal1");
        return getTerminals().get(0);
    }

    @Override
    public Optional<Terminal> getTerminal2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal2");
        if (terminals.size() > 1) {
            return Optional.of(terminals.get(1));
        }
        return Optional.empty();
    }

    @Override
    public DcTerminal getDcTerminal1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcTerminal1");
        return this.dcTerminals.get(0);
    }

    @Override
    public DcTerminal getDcTerminal2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcTerminal2");
        return this.dcTerminals.get(1);
    }

    @Override
    public List<DcTerminal> getDcTerminals() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcTerminals");
        return List.copyOf(dcTerminals);
    }

    void addDcTerminal(DcTerminalImpl dcTerminal) {
        this.dcTerminals.add(dcTerminal);
        dcTerminal.setDcConnectable(this);
    }

    @Override
    public double getIdleLoss() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "idleLoss");
        return this.idleLoss;
    }

    @Override
    public I setIdleLoss(double idleLoss) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, "idleLoss");
        ValidationUtil.checkDoubleParamPositive(this, idleLoss, "idleLoss");
        double oldValue = this.idleLoss;
        this.idleLoss = idleLoss;
        getNetwork().getListeners().notifyUpdate(this, "idleLoss", oldValue, idleLoss);
        return (I) this;
    }

    @Override
    public double getSwitchingLoss() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "switchingLoss");
        return this.switchingLoss;
    }

    @Override
    public I setSwitchingLoss(double switchingLoss) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, "switchingLoss");
        ValidationUtil.checkDoubleParamPositive(this, switchingLoss, "switchingLoss");
        double oldValue = this.switchingLoss;
        this.switchingLoss = switchingLoss;
        getNetwork().getListeners().notifyUpdate(this, "switchingLoss", oldValue, switchingLoss);
        return (I) this;
    }

    @Override
    public double getResistiveLoss() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "resistiveLoss");
        return this.resistiveLoss;
    }

    @Override
    public I setResistiveLoss(double resistiveLoss) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, "resistiveLoss");
        ValidationUtil.checkDoubleParamPositive(this, resistiveLoss, "resistiveLoss");
        double oldValue = this.resistiveLoss;
        this.resistiveLoss = resistiveLoss;
        getNetwork().getListeners().notifyUpdate(this, "resistiveLoss", oldValue, resistiveLoss);
        return (I) this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        for (DcTerminalImpl t : dcTerminals) {
            t.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        for (DcTerminalImpl t : dcTerminals) {
            t.reduceVariantArraySize(number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);

        for (DcTerminalImpl t : dcTerminals) {
            t.deleteVariantArrayElement(index);
        }
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (DcTerminalImpl t : dcTerminals) {
            t.allocateVariantArrayElement(indexes, sourceIndex);
        }
    }

    @Override
    public void remove() {
        super.remove();
        dcTerminals.forEach(DcTerminal::remove);
    }
}
