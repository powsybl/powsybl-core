/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.DcLine;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineImpl extends AbstractDcConnectable<DcLine> implements DcLine {

    private double r;

    DcLineImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, double r) {
        super(ref, subnetworkRef, id, name, fictitious);
        this.r = r;
    }

    @Override
    protected String getTypeDescription() {
        return "DC Line";
    }

    @Override
    public DcTerminal getDcTerminal1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal1");
        return this.dcTerminals.get(0);
    }

    @Override
    public DcTerminal getDcTerminal2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal2");
        return this.dcTerminals.get(1);
    }

    @Override
    public DcTerminal getDcTerminal(TwoSides side) {
        Objects.requireNonNull(side);
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal");
        if (side == TwoSides.ONE) {
            return this.dcTerminals.get(0);
        } else if (side == TwoSides.TWO) {
            return this.dcTerminals.get(1);
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public TwoSides getSide(DcTerminal dcTerminal) {
        Objects.requireNonNull(dcTerminal);
        if (getDcTerminal1() == dcTerminal) {
            return TwoSides.ONE;
        } else if (getDcTerminal2() == dcTerminal) {
            return TwoSides.TWO;
        } else {
            throw new IllegalStateException("The DC terminal is not connected to this DC line");
        }
    }

    @Override
    public double getR() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "r");
        return this.r;
    }

    @Override
    public DcLine setR(double r) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, "r");
        ValidationUtil.checkRPositive(this, r);
        double oldValue = this.r;
        this.r = r;
        getNetwork().getListeners().notifyUpdate(this, "r", oldValue, r);
        return this;
    }
}
