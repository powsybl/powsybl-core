/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class EmptyContingencyListProvider implements ContingenciesProvider {

    @Override
    public List<Contingency> getContingencies(Network network) {
        return Collections.emptyList();
    }
}
