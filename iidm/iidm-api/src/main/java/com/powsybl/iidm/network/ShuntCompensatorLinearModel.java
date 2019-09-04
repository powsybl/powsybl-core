/**
 * Copyright (c) 2019, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface ShuntCompensatorLinearModel extends ShuntCompensatorModel {

    /**
     * Get the susceptance per section in S.
     */
    double getbPerSection();

    /**
     * Set the susceptance per section in S.
     *
     * @param bPerSection the susceptance per section
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensatorLinearModel setbPerSection(double bPerSection);

    /**
     * Set the maximum number of section.
     *
     * @param maximumSectionCount the maximum number of section
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount);

    /**
     * Get the susceptance for the maximum section count.
     */
    double getMaximumB();
}
