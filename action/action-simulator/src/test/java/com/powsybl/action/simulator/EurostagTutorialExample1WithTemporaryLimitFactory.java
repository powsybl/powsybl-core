/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface EurostagTutorialExample1WithTemporaryLimitFactory {

    static Network create() {
        Network network = EurostagTutorialExample1Factory.create();
        // add a temporary limit
        Line l2 = network.getLine("NHV1_NHV2_2");
        l2.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();
        return network;
    }

}
