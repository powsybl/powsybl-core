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
public interface MutualCoupling {

    Line getLine1();

    void setLine1(Line line1);

    Line getLine2();

    void setLine2(Line line2);

    double getR();

    void setR(double r);

    double getX();

    void setX(double x);

    double getLine1Start();

    void setLine1Start(double line1Start);

    double getLine2Start();

    void setLine2Start(double line2Start);

    double getLine1End();

    void setLine1End(double line1End);

    double getLine2End();

    void setLine2End(double line2End);

}
