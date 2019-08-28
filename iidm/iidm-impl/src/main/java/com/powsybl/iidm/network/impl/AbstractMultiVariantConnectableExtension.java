/**
 * Copyright (c) 2019, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.exceptions.UncheckedClassCastExceptionException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public abstract class AbstractMultiVariantConnectableExtension<T extends Connectable> extends AbstractExtension<T> implements MultiVariantObject {

    public AbstractMultiVariantConnectableExtension(T extendable) {
        super(extendable);
    }

    protected VariantManagerHolder getVariantManagerHolder() {
        Network network = ((Terminal) getExtendable().getTerminals().get(0)).getVoltageLevel().getSubstation().getNetwork();

        if (network instanceof VariantManagerHolder) {
            return (VariantManagerHolder) network;
        }

        throw new UncheckedClassCastExceptionException("network cannot be converted to VariantManagerHolder");
    }

    protected int getVariantIndex() {
        return getVariantManagerHolder().getVariantIndex();
    }
}
