/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTerminal implements TerminalExt {

    protected Ref<? extends VariantManagerHolder> network;

    protected AbstractConnectable connectable;

    protected VoltageLevelExt voltageLevel;

    protected int num = -1;

    // attributes depending on the variant

    protected final TDoubleArrayList p;

    protected final TDoubleArrayList q;

    AbstractTerminal(Ref<? extends VariantManagerHolder> network) {
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
    public AbstractConnectable getConnectable() {
        return connectable;
    }

    @Override
    public void setConnectable(AbstractConnectable connectable) {
        this.connectable = connectable;
    }

    @Override
    public VoltageLevelExt getVoltageLevel() {
        return voltageLevel;
    }

    @Override
    public void setVoltageLevel(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public double getP() {
        return Optional.ofNullable(network).map(n -> p.get(n.get().getVariantIndex())).orElse(Double.NaN);
    }

    @Override
    public Terminal setP(double p) {
        if (network == null) {
            throw new PowsyblException("Can not modify on removed equipment");
        }
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            throw new ValidationException(connectable, "cannot set active power on a busbar section");
        }
        if (!Double.isNaN(p) && connectable.getType() == IdentifiableType.SHUNT_COMPENSATOR) {
            throw new ValidationException(connectable, "cannot set active power on a shunt compensator");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.p.set(variantIndex, p);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getConnectable().notifyUpdate(() -> "p" + (num != -1 ? num : ""), variantId, oldValue, p);
        return this;
    }

    @Override
    public double getQ() {
        return Optional.ofNullable(network).map(n -> q.get(n.get().getVariantIndex())).orElse(Double.NaN);
    }

    @Override
    public Terminal setQ(double q) {
        if (network == null) {
            throw new PowsyblException("Can not modify removed equipment");
        }
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            throw new ValidationException(connectable, "cannot set reactive power on a busbar section");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.q.set(variantIndex, q);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getConnectable().notifyUpdate(() -> "q" + (num != -1 ? num : ""), variantId, oldValue, q);
        return this;
    }

    protected abstract double getV();

    @Override
    public double getI() {
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            return 0;
        }
        int variantIndex = network.get().getVariantIndex();
        return Math.hypot(p.get(variantIndex), q.get(variantIndex))
                / (Math.sqrt(3.) * getV() / 1000);
    }

    @Override
    public boolean connect() {
        return voltageLevel.connect(this);
    }

    @Override
    public boolean disconnect() {
        return voltageLevel.disconnect(this);
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
        voltageLevel = null;
        network = null;
    }
}
