/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface ShuntCompensatorModel {

    /**
     * Get the susceptance in S of the section with a given section number if it exists.
     * Throw an exception if such a section does not exist.
     */
    double getB(int sectionNum);

    /**
     * Get the conductance in S of the section with a given section number if it exists.
     * Throw an exception if such a section does not exist.
     */
    double getG(int sectionNum);
}
