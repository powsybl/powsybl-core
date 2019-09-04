/**
 * Copyright (c) 2019, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Map;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface ShuntCompensatorNonLinearModel extends ShuntCompensatorModel {

    interface Section {

        /**
         * Get the susceptance of the section.
         */
        double getB();
    }

    /**
     * Get the shunt compensator's sections.
     */
    Map<Integer, Section> getSections();

    /**
     * Get the shunt compensator's section with the given number.
     */
    Section getSection(int sectionNum);

    ShuntCompensatorNonLinearModel addOrReplaceSection(int sectionNum, double b);

    ShuntCompensatorNonLinearModel removeSection(int sectionNum);
}
