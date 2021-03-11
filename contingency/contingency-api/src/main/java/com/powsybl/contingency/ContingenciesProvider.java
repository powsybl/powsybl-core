/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.iidm.network.Network;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ContingenciesProvider {

    List<Contingency> getContingencies(Network network);

    default String asScript() {
        throw new UnsupportedOperationException("Serialization not supported for contingencies provider of type " + this.getClass().getName());
    }

}
