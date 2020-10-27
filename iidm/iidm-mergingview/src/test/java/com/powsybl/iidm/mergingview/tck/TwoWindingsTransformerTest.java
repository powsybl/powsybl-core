/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractTwoWindingsTransformerTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TwoWindingsTransformerTest extends AbstractTwoWindingsTransformerTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("merge", "test");
        network.merge(NoEquipmentNetworkFactory.create());
        return network;
    }

    @Test
    @Ignore("FIXME (?) when new substation is created in merging view, it is not found when a branch is created linking a previously existing voltage level and a voltage level of this new substation")
    public void transformerNotInSameSubstation() {
        super.transformerNotInSameSubstation(); // FIXME (?) see ignore comment
    }
}
