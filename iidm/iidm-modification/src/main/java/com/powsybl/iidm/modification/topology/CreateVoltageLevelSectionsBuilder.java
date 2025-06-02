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

    private String bbsId;

    private boolean after = true;

    private boolean busbarOnly = false;

    private SwitchKind leftSwitchKind = SwitchKind.DISCONNECTOR;

    private SwitchKind rightSwitchKind = SwitchKind.DISCONNECTOR;

    private boolean leftSwitchFictitious = false;

    private boolean rightSwitchFictitious = false;

    public CreateVoltageLevelSections build() {
        return new CreateVoltageLevelSections(bbsId, after, busbarOnly, leftSwitchKind, rightSwitchKind, leftSwitchFictitious, rightSwitchFictitious);
    }

    public CreateVoltageLevelSectionsBuilder withBbsId(String bbsId) {
        this.bbsId = bbsId;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withAfter(boolean after) {
        this.after = after;
        return this;
    }

    public CreateVoltageLevelSectionsBuilder withBusbarOnly(boolean busbarOnly) {
        this.busbarOnly = busbarOnly;
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
