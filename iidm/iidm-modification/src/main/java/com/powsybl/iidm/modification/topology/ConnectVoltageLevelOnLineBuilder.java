/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.Line;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ConnectVoltageLevelOnLineBuilder {

    private double positionPercent = 50;

    private String bbsOrBusId = null;

    private String line1Id = null;
    private String line1Name = null;
    private String line2Id = null;
    private String line2Name = null;

    private Line line = null;

    public ConnectVoltageLevelOnLine build() {
        return new ConnectVoltageLevelOnLine(positionPercent, bbsOrBusId, line1Id, line1Name,
                line2Id, line2Name, line);
    }

    public ConnectVoltageLevelOnLineBuilder withPositionPercent(double positionPercent) {
        this.positionPercent = positionPercent;
        return this;
    }

    public ConnectVoltageLevelOnLineBuilder withBusbarSectionOrBusId(String bbsOrBusId) {
        this.bbsOrBusId = bbsOrBusId;
        return this;
    }

    public ConnectVoltageLevelOnLineBuilder withLine1Id(String line1Id) {
        this.line1Id = line1Id;
        return this;
    }

    public ConnectVoltageLevelOnLineBuilder withLine1Name(String line1Name) {
        this.line1Name = line1Name;
        return this;
    }

    public ConnectVoltageLevelOnLineBuilder withLine2Id(String line2Id) {
        this.line2Id = line2Id;
        return this;
    }

    public ConnectVoltageLevelOnLineBuilder withLine2Name(String line2Name) {
        this.line2Name = line2Name;
        return this;
    }

    public ConnectVoltageLevelOnLineBuilder withLine(Line line) {
        this.line = line;
        if (line1Id == null) {
            line1Id = line.getId() + "_1";
        }
        if (line2Id == null) {
            line2Id = line.getId() + "_2";
        }
        return this;
    }
}
