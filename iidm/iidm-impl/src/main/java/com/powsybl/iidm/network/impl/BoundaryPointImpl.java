/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BoundaryPoint;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.XnodeValuesComputation;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class BoundaryPointImpl implements BoundaryPoint {

    private static final String BOUNDARY_POINT = "boundaryPoint";

    private final Ref<? extends VariantManagerHolder> network;
    private final DanglingLineImpl parent;

    private final String side;

    private final TDoubleArrayList v;
    private final TDoubleArrayList angle;

    private final TDoubleArrayList p;
    private final TDoubleArrayList q;

    BoundaryPointImpl(DanglingLineImpl parent, String side, Ref<? extends VariantManagerHolder> network) {
        this.parent = Objects.requireNonNull(parent);
        this.side = Objects.requireNonNull(side);
        this.network = Objects.requireNonNull(network);
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        v = new TDoubleArrayList(variantArraySize);
        angle = new TDoubleArrayList(variantArraySize);
        p = new TDoubleArrayList(variantArraySize);
        q = new TDoubleArrayList(variantArraySize);
    }

    void initBoundaryPoint() {
        XnodeValuesComputation.computeAndSetXnodeValues(parent, sv -> {
            for (int i = 0; i < network.get().getVariantManager().getVariantArraySize(); i++) {
                v.add(sv.getU());
                angle.add(sv.getA());
                p.add(sv.getP());
                q.add(sv.getQ());
            }
        });
    }

    @Override
    public double getV() {
        return v.get(network.get().getVariantIndex());
    }

    void setV(double v) {
        if (v < 0) {
            throw new ValidationException(parent, "voltage of boundary point cannot be < 0");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.v.set(variantIndex, v);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        parent.notifyUpdate(() -> BOUNDARY_POINT + side + ".v", variantId, oldValue, v);
    }

    @Override
    public double getAngle() {
        return angle.get(network.get().getVariantIndex());
    }

    void setAngle(double angle) {
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.angle.set(variantIndex, angle);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        parent.notifyUpdate(() -> BOUNDARY_POINT + side + ".angle", variantId, oldValue, angle);
    }

    @Override
    public double getP() {
        return p.get(network.get().getVariantIndex());
    }

    void setP(double p) {
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.p.set(variantIndex, p);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        parent.notifyUpdate(() -> BOUNDARY_POINT + side + ".p", variantId, oldValue, p);
    }

    @Override
    public double getQ() {
        return q.get(network.get().getVariantIndex());
    }

    void setQ(double q) {
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.q.set(variantIndex, q);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        parent.notifyUpdate(() -> BOUNDARY_POINT + side + ".q", variantId, oldValue, q);
    }

    void computeAndSetBoundaryPoint() {
        XnodeValuesComputation.computeAndSetXnodeValues(parent, sv -> {
            setP(sv.getP());
            setQ(sv.getQ());
            setV(sv.getU());
            setAngle(sv.getA());
        });
    }

    void extendVariantArraySize(int number, int sourceIndex) {
        v.ensureCapacity(v.size() + number);
        angle.ensureCapacity(angle.size() + number);
        p.ensureCapacity(p.size() + number);
        q.ensureCapacity(q.size() + number);
        for (int i = 0; i < number; i++) {
            v.add(v.get(sourceIndex));
            angle.add(angle.get(sourceIndex));
            p.add(p.get(sourceIndex));
            q.add(q.get(sourceIndex));
        }
    }

    void reduceVariantArraySize(int number) {
        v.remove(v.size() - number, number);
        angle.remove(angle.size() - number, number);
        p.remove(p.size() - number, number);
        q.remove(q.size() - number, number);

    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            v.set(index, v.get(sourceIndex));
            angle.set(index, angle.get(sourceIndex));
            p.set(index, p.get(sourceIndex));
            q.set(index, q.get(sourceIndex));
        }
    }
}
