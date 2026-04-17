/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.Line;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface MutualCouplingAdder {

    MutualCouplingAdder withLine1(Line line1);

    MutualCouplingAdder withLine2(Line line2);

    MutualCouplingAdder withR(double r);

    MutualCouplingAdder withX(double x);

    MutualCouplingAdder withLine1Start(double start);

    MutualCouplingAdder withLine1End(double end);

    MutualCouplingAdder withLine2Start(double start);

    MutualCouplingAdder withLine2End(double end);

    MutualCoupling add();
}
