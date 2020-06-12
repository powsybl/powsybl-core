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
     * Get the section susceptance in S of the section with a given section index if it exists.
     *
     * @param sectionIndex index of the wanted section. Must be in [1;maximumSectionCount]. Else, throws a {@link ValidationException}.
     */
    double getBPerSection(int sectionIndex);

    /**
     * Get the section conductance in S of the section with a given section index if it exists.
     * If the section conductance has not been defined, return 0.
     *
     * @param sectionIndex index of the wanted section. Must be in [1; maximumSectionCount]. Else, throws a {@link ValidationException}.
     */
    double getGPerSection(int sectionIndex);
}
