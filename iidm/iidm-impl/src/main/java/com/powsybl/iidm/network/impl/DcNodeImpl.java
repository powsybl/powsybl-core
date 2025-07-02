/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcNodeImpl extends AbstractIdentifiable<DcNode> implements DcNode {

    public static final String NOMINAL_V_ATTRIBUTE = "nominalV";

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    protected boolean removed = false;
    private double nominalV;

    DcNodeImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, double nominalV) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.nominalV = nominalV;
    }

    @Override
    protected String getTypeDescription() {
        return "DC Node";
    }

    @Override
    public Network getParentNetwork() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "network");
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
    }

    @Override
    public NetworkImpl getNetwork() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "network");
        return networkRef.get();
    }

    @Override
    public double getNominalV() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, NOMINAL_V_ATTRIBUTE);
        return this.nominalV;
    }

    @Override
    public DcNode setNominalV(double nominalV) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, NOMINAL_V_ATTRIBUTE);
        ValidationUtil.checkNominalV(this, nominalV);
        double oldValue = this.nominalV;
        this.nominalV = nominalV;
        getNetwork().getListeners().notifyUpdate(this, NOMINAL_V_ATTRIBUTE, oldValue, nominalV);
        return this;
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();

        // this will be improved to be more efficient once the DC topology processor is implemented
        for (DcConnectable<?> dcConnectable : network.getDcConnectables()) {
            List<DcTerminal> dcTerminals = dcConnectable.getDcTerminals();
            for (DcTerminal dcTerminal : dcTerminals) {
                if (dcTerminal.getDcNode() == this) {
                    throw new PowsyblException("Cannot remove DC node '" + getId()
                            + "' because DC connectable '" + dcConnectable.getId() + "' is connected to it");
                }
            }
        }
        for (DcSwitch dcSwitch : network.getDcSwitches()) {
            if (dcSwitch.getDcNode1() == this || dcSwitch.getDcNode2() == this) {
                throw new PowsyblException("Cannot remove DC node '" + getId()
                        + "' because DC switch '" + dcSwitch.getId() + "' is connected to it");
            }
        }

        network.getListeners().notifyBeforeRemoval(this);

        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);

        removed = true;
    }
}
