/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.List;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface ShuntCompensatorNonLinearModel extends ShuntCompensatorModel {

    interface Section {

        /**
         * Get the accumulated susceptance in S if this section and all the previous ones are activated.
         */
        double getB();

        /**
         * Set the accumulated susceptance in S if this section and all the previous ones are activated.
         */
        Section setB(double b);

        /**
         * Get the accumulated conductance in S if this section and all the previous ones are activated.
         */
        double getG();

        /**
         * Set the accumulated conductance in S if this section and all the previous ones are activated.
         */
        Section setG(double g);
    }

    /**
     * Get all the sections as a list.
     */
    List<Section> getAllSections();
}
