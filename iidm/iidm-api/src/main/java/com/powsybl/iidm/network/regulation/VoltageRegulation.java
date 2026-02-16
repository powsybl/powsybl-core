/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulation {

    double getTargetValue();

    double setTargetValue(double targetValue);

    double getTargetDeadband();

    double setTargetDeadband(double targetDeadband);

    double getSlope();

    double setSlope(double slope);

    Terminal getTerminal();

    void setTerminal(Terminal terminal);

    RegulationMode getMode();

    void setMode(RegulationMode mode);

    boolean isRegulating();

    boolean setRegulating(boolean regulating);

    void removeTerminal();
}
