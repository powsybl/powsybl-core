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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractDcConverter<I extends DcConverter<I>> extends AbstractConnectable<I> implements DcConverter<I>, MultiVariantObject {

    protected final List<DcTerminal> dcTerminals = new ArrayList<>();

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
        return getTerminals().get(0);
    }

    @Override
    public Optional<Terminal> getTerminal2() {
        if (terminals.size() > 1) {
            return Optional.of(terminals.get(1));
        }
        return Optional.empty();
    }

    @Override
    public DcTerminal getDcTerminal1() {
        return this.dcTerminals.get(0);
    }

    @Override
    public DcTerminal getDcTerminal2() {
        return this.dcTerminals.get(1);
    }

    @Override
    public List<DcTerminal> getDcTerminals() {
        return this.dcTerminals;
    }

    void addDcTerminal(DcTerminalImpl dcTerminal) {
        this.dcTerminals.add(dcTerminal);
        dcTerminal.setDcConnectable(this);
    }

    @Override
    public double getIdleLoss() {
        return this.idleLoss;
    }

    @Override
    public I setIdleLoss(double idleLoss) {
        // todo: validation / positive
        this.idleLoss = idleLoss;
        return (I) this;
    }

    @Override
    public double getSwitchingLoss() {
        return this.switchingLoss;
    }

    @Override
    public I setSwitchingLoss(double switchingLoss) {
        // todo: validation / positive
        this.switchingLoss = switchingLoss;
        return (I) this;
    }

    @Override
    public double getResistiveLoss() {
        return this.resistiveLoss;
    }

    @Override
    public I setResistiveLoss(double resistiveLoss) {
        // todo: validation / positive
        this.resistiveLoss = resistiveLoss;
        return (I) this;
    }
}
