/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractScalable implements Scalable {

    protected AbstractScalable() {
    }

    @Override
    public List<Generator> listGenerators(Network n, List<String> notFoundGenerators) {
        List<Generator> generators = new ArrayList<>();
        listGenerators(n, generators, notFoundGenerators);
        return generators;
    }

    @Override
    public List<Generator> listGenerators(Network n) {
        return listGenerators(n, null);
    }
}
