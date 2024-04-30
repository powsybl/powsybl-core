/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class RevertConnectVoltageLevelOnLineBuilder {

    private String line1Id = null;
    private String line2Id = null;

    private String lineId = null;
    private String lineName = null;

    public RevertConnectVoltageLevelOnLine build() {
        return new RevertConnectVoltageLevelOnLine(line1Id, line2Id, lineId, lineName);
    }

    /**
     * @param line1Id       The non-null ID of the first line
     */
    public RevertConnectVoltageLevelOnLineBuilder withLine1Id(String line1Id) {
        this.line1Id = line1Id;
        return this;
    }

    /**
     * @param line2Id       The non-null ID of the second line
     */
    public RevertConnectVoltageLevelOnLineBuilder withLine2Id(String line2Id) {
        this.line2Id = line2Id;
        return this;
    }

    /**
     * @param lineId        The non-null ID of the new line to be created
     */
    public RevertConnectVoltageLevelOnLineBuilder withLineId(String lineId) {
        this.lineId = lineId;
        return this;
    }

    /**
     * @param lineName      The optional name of the new line to be created
     */
    public RevertConnectVoltageLevelOnLineBuilder withLineName(String lineName) {
        this.lineName = lineName;
        return this;
    }
}
