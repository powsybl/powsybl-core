/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadModel;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadImpl extends AbstractConnectable<Load> implements Load {

    private final Ref<? extends VariantManagerHolder> network;

    private LoadType loadType;

    private LoadModel model;

    // attributes depending on the variant

    private final ExtendedDoubleArrayList p0;

    private final ExtendedDoubleArrayList q0;

    LoadImpl(Ref<NetworkImpl> networkRef,
             String id, String name, boolean fictitious, LoadType loadType, LoadModel model,
             double p0, double q0) {
        super(networkRef, id, name, fictitious);
        this.network = networkRef;
        this.loadType = loadType;
        this.model = model;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.p0 = new ExtendedDoubleArrayList(variantArraySize, p0);
        this.q0 = new ExtendedDoubleArrayList(variantArraySize, q0);
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    protected String getTypeDescription() {
        return "Load";
    }

    @Override
    public LoadType getLoadType() {
        return loadType;
    }

    @Override
    public Load setLoadType(LoadType loadType) {
        ValidationUtil.checkLoadType(this, loadType);
        LoadType oldValue = this.loadType;
        this.loadType = loadType;
        notifyUpdate("loadType", oldValue.toString(), loadType.toString());
        return this;
    }

    @Override
    public double getP0() {
        return p0.getDouble(network.get().getVariantIndex());
    }

    @Override
    public LoadImpl setP0(double p0) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkP0(this, p0, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.p0.set(variantIndex, p0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return q0.getDouble(network.get().getVariantIndex());
    }

    @Override
    public LoadImpl setQ0(double q0) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkQ0(this, q0, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.q0.set(variantIndex, q0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("q0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public Optional<LoadModel> getModel() {
        return Optional.ofNullable(model);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        p0.growAndFill(number, p0.getDouble(sourceIndex));
        q0.growAndFill(number, q0.getDouble(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        p0.removeElements(number);
        q0.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            p0.set(index, p0.getDouble(sourceIndex));
            q0.set(index, q0.getDouble(sourceIndex));
        }
    }

}
