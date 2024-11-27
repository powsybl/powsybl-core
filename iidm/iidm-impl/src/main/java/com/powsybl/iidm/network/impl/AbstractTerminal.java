/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractTerminal implements TerminalExt {

    protected static final String UNMODIFIABLE_REMOVED_EQUIPMENT = "Cannot modify removed equipment ";
    protected static final String CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT = "Cannot access bus of removed equipment ";

    private Ref<? extends VariantManagerHolder> network;

    protected final ThreeSides side;

    protected AbstractConnectable connectable;

    protected VoltageLevelExt voltageLevel;

    protected final ReferrerManager<Terminal> referrerManager = new ReferrerManager<>(this);

    // attributes depending on the variant

    protected final TDoubleArrayList p;

    protected final TDoubleArrayList q;

    protected boolean removed = false;

    AbstractTerminal(Ref<? extends VariantManagerHolder> network, ThreeSides side) {
        this.side = side;
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        p = new TDoubleArrayList(variantArraySize);
        q = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            p.add(Double.NaN);
            q.add(Double.NaN);
        }
    }

    @Override
    public ThreeSides getSide() {
        return side;
    }

    protected String getAttributeSideSuffix() {
        return "" + (side != null ? side.getNum() : "");
    }

    protected VariantManagerHolder getVariantManagerHolder() {
        return network.get();
    }

    @Override
    public AbstractConnectable getConnectable() {
        return connectable;
    }

    @Override
    public void setConnectable(AbstractConnectable connectable) {
        this.connectable = connectable;
    }

    @Override
    public VoltageLevelExt getVoltageLevel() {
        if (removed) {
            throw new PowsyblException("Cannot access voltage level of removed equipment " + connectable.id);
        }
        return voltageLevel;
    }

    @Override
    public void setVoltageLevel(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
        if (voltageLevel != null) {
            network = voltageLevel.getNetworkRef();
        }
    }

    @Override
    public double getP() {
        if (removed) {
            throw new PowsyblException("Cannot access p of removed equipment " + connectable.id);
        }
        return p.get(network.get().getVariantIndex());
    }

    @Override
    public Terminal setP(double p) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            throw new ValidationException(connectable, "cannot set active power on a busbar section");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.p.set(variantIndex, p);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getConnectable().notifyUpdate(() -> "p" + getAttributeSideSuffix(), variantId, oldValue, p);
        return this;
    }

    @Override
    public double getQ() {
        if (removed) {
            throw new PowsyblException("Cannot access q of removed equipment " + connectable.id);
        }
        return q.get(network.get().getVariantIndex());
    }

    @Override
    public Terminal setQ(double q) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            throw new ValidationException(connectable, "cannot set reactive power on a busbar section");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.q.set(variantIndex, q);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getConnectable().notifyUpdate(() -> "q" + getAttributeSideSuffix(), variantId, oldValue, q);
        return this;
    }

    protected abstract double getV();

    @Override
    public double getI() {
        if (removed) {
            throw new PowsyblException("Cannot access i of removed equipment " + connectable.id);
        }
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            return 0;
        }
        int variantIndex = network.get().getVariantIndex();
        return Math.hypot(p.get(variantIndex), q.get(variantIndex))
                / (Math.sqrt(3.) * getV() / 1000);
    }

    @Override
    public boolean connect() {
        return connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER);
    }

    /**
     * Try to connect the terminal.<br/>
     * Depends on the working variant.
     * @param isTypeSwitchToOperate Predicate telling if a switch is considered operable. Examples of predicates are available in the class {@link SwitchPredicates}
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        boolean connectedBefore = isConnected();
        connectable.notifyUpdate("beginConnect", variantId, connectedBefore, null);
        boolean connected = voltageLevel.getTopologyModel().connect(this, isTypeSwitchToOperate);
        boolean connectedAfter = isConnected();
        connectable.notifyUpdate("endConnect", variantId, null, connectedAfter);
        return connected;
    }

    @Override
    public boolean disconnect() {
        return disconnect(SwitchPredicates.IS_CLOSED_BREAKER);
    }

    /**
     * Disconnect the terminal.<br/>
     * Depends on the working variant.
     * @param isSwitchOpenable Predicate telling if a switch is considered openable. Examples of predicates are available in the class {@link SwitchPredicates}
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        boolean disconnectedBefore = !isConnected();
        connectable.notifyUpdate("beginDisconnect", variantId, disconnectedBefore, null);
        boolean disconnected = voltageLevel.getTopologyModel().disconnect(this, isSwitchOpenable);
        boolean disconnectedAfter = !isConnected();
        connectable.notifyUpdate("endDisconnect", variantId, null, disconnectedAfter);
        return disconnected;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        p.ensureCapacity(p.size() + number);
        q.ensureCapacity(q.size() + number);
        for (int i = 0; i < number; i++) {
            p.add(p.get(sourceIndex));
            q.add(q.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        p.remove(p.size() - number, number);
        q.remove(q.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            p.set(index, p.get(sourceIndex));
            q.set(index, q.get(sourceIndex));
        }
    }

    @Override
    public void remove() {
        removed = true;
    }

    @Override
    public ReferrerManager<Terminal> getReferrerManager() {
        return referrerManager;
    }
}
