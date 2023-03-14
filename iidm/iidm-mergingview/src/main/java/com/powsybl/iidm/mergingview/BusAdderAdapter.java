/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusAdderAdapter extends AbstractIdentifiableAdderAdapter<Bus, BusAdder> implements BusAdder {

    BusAdderAdapter(final BusAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Bus add() {
        checkAndSetUniqueId();
        return getIndex().getBus(getDelegate().add());
    }
}
