/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;

import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface CgmesControlArea {

    String getId();

    String getName();

    String getEnergyIdentificationCodeEIC();

    double getNetInterchange();

    void add(Terminal terminal);

    void add(Boundary boundary);

    Set<Terminal> getTerminals();

    Set<Boundary> getBoundaries();
}
