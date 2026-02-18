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

    /**
     * TODO MSA JAVADOC
     */
    double getTargetValue();

    /**
     * TODO MSA JAVADOC
     */
    double setTargetValue(double targetValue);

    /**
     * TODO MSA JAVADOC
     */
    double getTargetDeadband();

    /**
     * TODO MSA JAVADOC
     */
    double setTargetDeadband(double targetDeadband);

    /**
     * TODO MSA JAVADOC
     */
    double getSlope();

    /**
     * TODO MSA JAVADOC
     */
    double setSlope(double slope);

    /**
     * TODO MSA JAVADOC
     */
    Terminal getTerminal();

    /**
     * TODO MSA JAVADOC
     */
    void setTerminal(Terminal terminal);

    /**
     * TODO MSA JAVADOC
     */
    RegulationMode getMode();

    /**
     * TODO MSA JAVADOC
     */
    void setMode(RegulationMode mode);

    /**
     * TODO MSA JAVADOC
     */
    boolean isRegulating();

    /**
     * TODO MSA JAVADOC
     */
    boolean setRegulating(boolean regulating);

    /**
     * TODO MSA JAVADOC
     */
    void removeTerminal();
}
