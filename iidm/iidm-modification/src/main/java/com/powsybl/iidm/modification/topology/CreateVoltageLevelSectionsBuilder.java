/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.SwitchKind;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer_externe at rte-france.com>}
 */
public class CreateVoltageLevelSectionsBuilder {

    private String referenceBusbarSectionId;

    private boolean createTheBusbarSectionsAfterTheReferenceBusbarSection = true;

    private boolean allBusbars = true;

    private SwitchKind leftSwitchKind = SwitchKind.DISCONNECTOR;

    private SwitchKind rightSwitchKind = SwitchKind.DISCONNECTOR;

    private boolean leftSwitchFictitious = false;

    private boolean rightSwitchFictitious = false;

    public CreateVoltageLevelSections build() {
        return new CreateVoltageLevelSections(referenceBusbarSectionId, createTheBusbarSectionsAfterTheReferenceBusbarSection, allBusbars, leftSwitchKind, rightSwitchKind, leftSwitchFictitious, rightSwitchFictitious);
    }

    public CreateVoltageLevelSectionsBuilder withReferenceBusbarSectionId(String referenceBusbarSectionId) {
        this.referenceBusbarSectionId = referenceBusbarSectionId;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(boolean createTheBusbarSectionsAfterTheReferenceBusbarSection) {
        this.createTheBusbarSectionsAfterTheReferenceBusbarSection = createTheBusbarSectionsAfterTheReferenceBusbarSection;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withAllBusbars(boolean allBusbars) {
        this.allBusbars = allBusbars;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withLeftSwitchKind(SwitchKind leftSwitchKind) {
        this.leftSwitchKind = leftSwitchKind;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withRightSwitchKind(SwitchKind rightSwitchKind) {
        this.rightSwitchKind = rightSwitchKind;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withLeftSwitchFictitious(boolean leftSwitchFictitious) {
        this.leftSwitchFictitious = leftSwitchFictitious;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withRightSwitchFictitious(boolean rightSwitchFictitious) {
        this.rightSwitchFictitious = rightSwitchFictitious;
        return this;
    }
}
