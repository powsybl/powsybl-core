/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.scripting

import com.powsybl.commons.PowsyblException
import com.powsybl.iidm.network.ShuntCompensator
import com.powsybl.iidm.network.ShuntCompensatorLinearModel
import com.powsybl.iidm.network.ShuntCompensatorModelType
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel

/**
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ShuntCompensatorExtension {

    static double getMaximumB(ShuntCompensator self) {
        if (ShuntCompensatorModelType.LINEAR == self.getModelType()) {
            // forward bPerSection * maximumSectionCount for linear shunts
            return self.getModel(ShuntCompensatorLinearModel.class).getBPerSection() * self.getModel().getMaximumSectionCount()
        }
        throw new PowsyblException("shunt model is not linear")
    }

    static double getbPerSection(ShuntCompensator self) {
        if (ShuntCompensatorModelType.LINEAR == self.getModelType()) {
            // forward bPerSection of linear model
            return self.getModel(ShuntCompensatorLinearModel.class).getBPerSection()
        }
        throw new PowsyblException("shunt model is not linear")
    }

    static void setbPerSection(ShuntCompensator self, double bPerSection) {
        if (ShuntCompensatorModelType.LINEAR == self.getModelType()) {
            // forward to linear model setter for bPerSection
            self.getModel(ShuntCompensatorLinearModel.class).setBPerSection(bPerSection)
        } else {
            throw new PowsyblException("shunt model is not linear")
        }
    }

    static int getMaximumSectionCount(ShuntCompensator self) {
        // forward maximumSectionCount of model
        self.getModel().getMaximumSectionCount()
    }

    static void setMaximumSectionCount(ShuntCompensator self, int maximumSectionCount) {
        if (ShuntCompensatorModelType.LINEAR == self.getModelType()) {
            // forward to linear model setter for maximumSectionCount
            self.getModel(ShuntCompensatorLinearModel.class).setMaximumSectionCount(maximumSectionCount)
        } else {
            throw new PowsyblException("shunt model is not linear")
        }
    }

    static int getCurrentSectionCount(ShuntCompensator self) {
        self.getSectionCount()
    }

    static void setCurrentSectionCount(ShuntCompensator self, int currentSectionCount) {
        self.setSectionCount(currentSectionCount)
    }

    static double getCurrentB(ShuntCompensator self) {
        self.getB()
    }
}
