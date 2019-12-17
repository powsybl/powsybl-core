/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ShuntCompensatorToShuntCompensator extends IidmToCgmes {

    ShuntCompensatorToShuntCompensator() {
        ignore("q");

        simpleUpdate("bPerSection", "cim:LinearShuntCompensator.bPerSection", CgmesSubset.EQUIPMENT);
        simpleUpdate("maximumSectionCount", "cim:ShuntCompensator.maximumSections", CgmesSubset.EQUIPMENT);
        simpleUpdate("currentSectionCount", "cim:ShuntCompensator.sections", CgmesSubset.STEADY_STATE_HYPOTHESIS);
    }

}
