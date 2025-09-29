/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CoordinatedReactiveControlImpl extends AbstractMultiVariantIdentifiableExtension<Generator> implements CoordinatedReactiveControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatedReactiveControlImpl.class);
    private final ExtendedDoubleArrayList qPercent;

    public CoordinatedReactiveControlImpl(Generator generator, double qPercent) {
        super(generator);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.qPercent = new ExtendedDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.qPercent.add(checkQPercent(generator, qPercent));
        }
    }

    @Override
    public double getQPercent() {
        return qPercent.getDouble(getVariantIndex());
    }

    @Override
    public void setQPercent(double qPercent) {
        this.qPercent.set(getVariantIndex(), checkQPercent(getExtendable(), qPercent));
    }

    private static double checkQPercent(Generator generator, double qPercent) {
        if (Double.isNaN(qPercent)) {
            throw new PowsyblException(String.format("Undefined value (%s) for qPercent for generator %s",
                qPercent, generator.getId()));
        }
        if (qPercent < 0 || qPercent > 100) {
            LOGGER.debug("qPercent value of generator {} does not seem to be a valid percent: {}", generator.getId(), qPercent);
        }
        return qPercent;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        qPercent.growAndFill(number, qPercent.getDouble(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        qPercent.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            qPercent.set(index, qPercent.getDouble(sourceIndex));
        }
    }
}
