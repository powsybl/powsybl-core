/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Map;
import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface ShuntCompensatorNonLinearModel extends ShuntCompensatorModel {

    interface Section {

        /**
         * Get the susceptance in S of the section.
         */
        double getB();

        /**
         * Get the conductance in S of the section.
         */
        double getG();
    }

    /**
     * Get the maximum susceptance for a section in S.
     */
    double getMaximumB();

    /**
     * Get the maximum conductance for a section in S.
     */
    double getMaximumG();

    /**
     * Get an optional of the section associated with a given section number if it exists.
     * If such a section does not exist, return an empty optional.
     *
     */
    Optional<Section> getSection(int sectionNum);

    /**
     * Get all the sections associated with their section number.
     */
    Map<Integer, Section> getSections();

    /**
     * For a given section number, add a section with a given susceptance and conductance in S to the model.
     * If a section already exists for this section number, respectively replace its susceptance and conductance with the given susceptance and conductance.
     */
    ShuntCompensatorNonLinearModel addOrReplaceSection(int sectionNum, double b, double g);

    /**
     * Remove the section associated with a given section number if it exists <b>and</b> the current section count is different of the given section number.
     * Else, throw an exception.
     */
    ShuntCompensatorNonLinearModel removeSection(int sectionNum);
}
