/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.HvdcConverterStationAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractHvdcConverterStationAdderAdapter<I extends HvdcConverterStationAdder<I>> extends AbstractInjectionAdderAdapter<I> implements HvdcConverterStationAdder<I> {

    protected AbstractHvdcConverterStationAdderAdapter(I delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public I setLossFactor(float lossFactor) {
        getDelegate().setLossFactor(lossFactor);
        return (I) this;
    }
}
