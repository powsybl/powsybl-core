/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface ShuntCompensatorLinearModel extends ShuntCompensatorModel {

    /**
     * Get the susceptance per section in S.
     */
    double getBPerSection();

    /**
     * Set the susceptance per section in S.
     */
    ShuntCompensatorLinearModel setBPerSection(double bPerSection);

    /**
     * Get the conductance per section in S.
     */
    double getGPerSection();

    /**
     * Set the conductance per section in S.
     */
    ShuntCompensatorLinearModel setGPerSection(double gPerSection);

    /**
     * Set the maximum number of sections.
     */
    ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount);
}
