/**
 * Copyright (c) 2020, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.exceptions.UncheckedClassCastExceptionException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractMultiVariantIdentifiableExtension<T extends Identifiable<T>> extends AbstractExtension<T> implements MultiVariantObject {

    public AbstractMultiVariantIdentifiableExtension(T extendable) {
        super(extendable);
    }

    protected VariantManagerHolder getVariantManagerHolder() {
        Network network = getExtendable().getNetwork();

        if (network instanceof VariantManagerHolder variantManagerHolder) {
            return variantManagerHolder;
        }

        throw new UncheckedClassCastExceptionException("network cannot be converted to VariantManagerHolder");
    }

    protected int getVariantIndex() {
        return getVariantManagerHolder().getVariantIndex();
    }

}
