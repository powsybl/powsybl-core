/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
enum LimitsXmlParsingState {
    UNDER,
    OVER;

    static void addGenerator(LimitsXmlParsingState state, String generatorId, List<String> onUnderDiconnectedGenerators, List<String> onOverDiconnectedGenerators) {
        if (state == null) {
            throw new IllegalStateException();
        }
        switch (state) {
            case UNDER:
                onUnderDiconnectedGenerators.add(generatorId);
                break;

            case OVER:
                onOverDiconnectedGenerators.add(generatorId);
                break;

            default:
                throw new AssertionError();
        }
    }
}
