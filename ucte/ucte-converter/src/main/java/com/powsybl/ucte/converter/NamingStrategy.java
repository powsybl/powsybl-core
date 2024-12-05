/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public interface NamingStrategy {

    void initialiseNetwork(Network network);

    String getName();

    UcteNodeCode getUcteNodeCode(String id);

    UcteNodeCode getUcteNodeCode(Bus bus);

    UcteNodeCode getUcteNodeCode(DanglingLine danglingLine);

    UcteElementId getUcteElementId(String id);

    UcteElementId getUcteElementId(Switch sw);

    UcteElementId getUcteElementId(Branch branch);

    UcteElementId getUcteElementId(DanglingLine danglingLine);
}
