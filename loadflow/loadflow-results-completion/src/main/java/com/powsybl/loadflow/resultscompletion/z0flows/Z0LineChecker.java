/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.iidm.network.Line;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface Z0LineChecker {
    public boolean isZ0(Line line);
}
