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
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcNodeImpl extends AbstractDcTopologyVisitable<DcNode> implements DcNode, MultiVariantObject {

    public static final String NOMINAL_V_ATTRIBUTE = "nominalV";

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    protected boolean removed = false;
    private double nominalV;

    private final List<DcTerminal> dcTerminals;

    private final TDoubleArrayList v;

    private final TIntArrayList connectedComponentNumber;

    private final TIntArrayList dcComponentNumber;

    DcNodeImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, double nominalV) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.nominalV = nominalV;
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        dcTerminals = new ArrayList<>();
        v = new TDoubleArrayList(variantArraySize);
        connectedComponentNumber = new TIntArrayList(variantArraySize);
        dcComponentNumber = new TIntArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            v.add(Double.NaN);
            connectedComponentNumber.add(-1);
            dcComponentNumber.add(-1);
        }
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
    public DcBus getDcBus() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcBus");
        return ((AbstractNetwork) getParentNetwork()).getDcTopologyModel().getDcBusOfDcNode(getId());
    }

    @Override
    public double getV() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "v");
        return v.get(getNetwork().getVariantIndex());
    }

    @Override
    public DcNode setV(double v) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, "v");
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.v.set(variantIndex, v);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        getNetwork().getListeners().notifyUpdate(this, "v", variantId, oldValue, v);
        return this;
    }

    public void addDcTerminal(DcTerminalImpl dcTerminal) {
        dcTerminals.add(dcTerminal);
    }

    public void removeDcTerminal(DcTerminalImpl dcTerminal) {
        if (!dcTerminals.remove(dcTerminal)) {
            throw new IllegalStateException("DcTerminal " + dcTerminal + " not found");
        }
    }

    public void setConnectedComponentNumber(int connectedComponentNumber) {
        int variantIndex = networkRef.get().getVariantIndex();
        int oldValue = this.connectedComponentNumber.set(variantIndex, connectedComponentNumber);
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        networkRef.get().getListeners().notifyUpdate(this, "connectedComponentNumber", variantId, oldValue, connectedComponentNumber);
    }

    public Component getConnectedComponent() {
        NetworkImpl.ConnectedComponentsManager ccm = networkRef.get().getConnectedComponentsManager();
        ccm.update();
        return ccm.getComponent(connectedComponentNumber.get(networkRef.get().getVariantIndex()));
    }

    public void setDcComponentNumber(int componentNumber) {
        int variantIndex = networkRef.get().getVariantIndex();
        int oldValue = this.dcComponentNumber.set(variantIndex, componentNumber);
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        networkRef.get().getListeners().notifyUpdate(this, "dcComponentNumber", variantId, oldValue, dcComponentNumber);
    }

    public Component getDcComponent() {
        NetworkImpl.DcComponentsManager dcm = networkRef.get().getDcComponentsManager();
        dcm.update();
        return dcm.getComponent(dcComponentNumber.get(networkRef.get().getVariantIndex()));
    }

    @Override
    public int getDcTerminalCount() {
        return getDcTerminals().size();
    }

    @Override
    public List<DcTerminal> getDcTerminals() {
        return dcTerminals;
    }

    @Override
    public Stream<DcTerminal> getDcTerminalStream() {
        return getDcTerminals().stream();
    }

    @Override
    public int getConnectedDcTerminalCount() {
        return (int) getConnectedDcTerminalStream().count();
    }

    @Override
    public List<DcTerminal> getConnectedDcTerminals() {
        return getConnectedDcTerminalStream().toList();
    }

    @Override
    public Stream<DcTerminal> getConnectedDcTerminalStream() {
        return getDcTerminalStream().filter(DcTerminal::isConnected);
    }

    @Override
    public void visitConnectedEquipments(DcTopologyVisitor visitor) {
        visitEquipments(getConnectedDcTerminals(), visitor);
    }

    @Override
    public void visitConnectedOrConnectableEquipments(DcTopologyVisitor visitor) {
        visitEquipments(getDcTerminals(), visitor);
    }

    static void visitEquipments(Iterable<DcTerminal> dcTerminals, DcTopologyVisitor visitor) {
        Objects.requireNonNull(visitor);
        for (DcTerminal dcTerminal : dcTerminals) {
            DcConnectable<?> dcConnectable = dcTerminal.getDcConnectable();
            switch (dcConnectable.getType()) {
                case DC_GROUND -> visitor.visitDcGround((DcGroundImpl) dcConnectable);
                case DC_LINE -> visitor.visitDcLine((DcLineImpl) dcConnectable, dcTerminal.getSide());
                case LINE_COMMUTATED_CONVERTER, VOLTAGE_SOURCE_CONVERTER -> {
                    AcDcConverter<?> converter = (AcDcConverter<?>) dcConnectable;
                    visitor.visitAcDcConverter(converter, dcTerminal.getTerminalNumber());
                }
                default -> throw new IllegalStateException();
            }
        }
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();

        List<DcTerminal> dcTerminalList = getDcTerminals();
        if (!dcTerminalList.isEmpty()) {
            throw new PowsyblException("Cannot remove DC node '" + getId()
                    + "' because DC connectable '" + dcTerminalList.get(0).getDcConnectable().getId() + "' is connected to it");
        }
        for (DcSwitch dcSwitch : network.getDcSwitches()) {
            if (dcSwitch.getDcNode1() == this || dcSwitch.getDcNode2() == this) {
                throw new PowsyblException("Cannot remove DC node '" + getId()
                        + "' because DC switch '" + dcSwitch.getId() + "' is connected to it");
            }
        }

        network.getListeners().notifyBeforeRemoval(this);

        network.getIndex().remove(this);
        ((AbstractNetwork) getParentNetwork()).getDcTopologyModel().removeDcNode(getId());

        network.getListeners().notifyAfterRemoval(id);

        removed = true;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        v.ensureCapacity(v.size() + number);
        connectedComponentNumber.ensureCapacity(connectedComponentNumber.size() + number);
        dcComponentNumber.ensureCapacity(dcComponentNumber.size() + number);
        for (int i = 0; i < number; i++) {
            v.add(v.get(sourceIndex));
            connectedComponentNumber.add(connectedComponentNumber.get(sourceIndex));
            dcComponentNumber.add(dcComponentNumber.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            v.removeAt(v.size() - 1);
            connectedComponentNumber.removeAt(connectedComponentNumber.size() - 1);
            dcComponentNumber.removeAt(dcComponentNumber.size() - 1);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            v.set(index, v.get(sourceIndex));
            connectedComponentNumber.set(index, connectedComponentNumber.get(sourceIndex));
            dcComponentNumber.set(index, dcComponentNumber.get(sourceIndex));
        }
    }
}
