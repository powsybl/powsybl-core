/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractDcConnectable<I extends DcConnectable<I>> extends AbstractIdentifiable<I> implements DcConnectable<I>, MultiVariantObject {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    protected final List<DcTerminal> dcTerminals = new ArrayList<>();
    protected boolean removed = false;

    AbstractDcConnectable(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
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
    public List<DcTerminal> getDcTerminals() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcTerminals");
        return this.dcTerminals;
    }

    void addDcTerminal(DcTerminalImpl dcTerminal) {
        this.dcTerminals.add(dcTerminal);
        dcTerminal.setDcConnectable(this);
    }

    public void remove() {
        NetworkImpl network = getNetwork();

        network.getListeners().notifyBeforeRemoval(this);

        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
        dcTerminals.forEach(DcTerminal::remove);
    }
}
