/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.identifiant;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public interface Identifier {
    Contingency filterIdentifiable(Network network);

    enum IdentifierType {
        SIMPLE,
        UCTE,
        LIST
    }

    IdentifierType getType();
}
