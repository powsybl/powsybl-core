/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.extension;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.NetworkImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicModelInfoImpl<I extends Identifiable<I>> extends AbstractMultiVariantIdentifiableExtension<I> implements DynamicModelInfo<I> {

    private final ArrayList<String> modelNamePerVariant;

    public DynamicModelInfoImpl(I extendable, String modelName) {
        super(extendable);
        this.modelNamePerVariant = new ArrayList<>(Collections.nCopies(
                getVariantManagerHolder().getVariantManager().getVariantArraySize(), null));
        this.modelNamePerVariant.set(getVariantIndex(), modelName);
    }

    @Override
    public String getModelName() {
        return modelNamePerVariant.get(getVariantIndex());
    }

    @Override
    public void setModelName(String modelName) {
        int variantIndex = getVariantIndex();
        String oldModelName = modelNamePerVariant.get(variantIndex);
        if (!Objects.equals(oldModelName, modelName)) {
            modelNamePerVariant.set(variantIndex, modelName);
            NetworkImpl network = (NetworkImpl) getExtendable().getNetwork();
            String variantId = getVariantManagerHolder().getVariantManager().getWorkingVariantId();
            network.getListeners().notifyExtensionUpdate(this, "modelName", variantId, oldModelName, modelName);
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        modelNamePerVariant.ensureCapacity(modelNamePerVariant.size() + number);
        for (int i = 0; i < number; ++i) {
            modelNamePerVariant.add(modelNamePerVariant.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            modelNamePerVariant.remove(modelNamePerVariant.size() - 1); // remove elements from the top to avoid moves inside the array
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        modelNamePerVariant.set(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        String sourceModelName = modelNamePerVariant.get(sourceIndex);
        for (int index : indexes) {
            modelNamePerVariant.set(index, sourceModelName);
        }
    }
}
