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
public interface ShuntCompensatorNonLinearModelAdder {

    interface SectionAdder {

        /**
         * Set the accumulated susceptance in S when the section to be added is the last section in service.
         */
        SectionAdder setB(double b);

        /***
         * Set the accumulated conductance is S when the section to be added is the last section in service.
         * If the accumulated conductance is undefined, its conductance per section is considered equal to 0:
         * it is equal to the accumulated conductance of the previous section.
         */
        SectionAdder setG(double g);

        ShuntCompensatorNonLinearModelAdder endSection();
    }

    SectionAdder beginSection();

    ShuntCompensatorAdder add();
}
