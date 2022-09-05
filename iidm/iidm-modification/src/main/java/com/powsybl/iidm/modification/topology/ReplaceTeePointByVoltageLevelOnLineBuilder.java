/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ReplaceTeePointByVoltageLevelOnLineBuilder {

    private String line1ZId = null;
    private String lineZ2Id = null;
    private String lineZPId = null;
    private String voltageLevelId = null;
    private String bbsOrBusId = null;
    private String line1CId = null;
    private String line1CName = null;
    private String lineC2Id = null;
    private String lineC2Name = null;

    public ReplaceTeePointByVoltageLevelOnLine build() {
        return new ReplaceTeePointByVoltageLevelOnLine(line1ZId, lineZ2Id, lineZPId, voltageLevelId, bbsOrBusId,
                                                       line1CId, line1CName, lineC2Id, lineC2Name);
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withLine1ZId(String line1ZId) {
        this.line1ZId = line1ZId;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withLineZ2Id(String lineZ2Id) {
        this.lineZ2Id = lineZ2Id;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withLineZPId(String lineZPId) {
        this.lineZPId = lineZPId;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withBbsOrBusId(String bbsOrBusId) {
        this.bbsOrBusId = bbsOrBusId;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withLine1CId(String line1CId) {
        this.line1CId = line1CId;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withLine1CName(String line1CName) {
        this.line1CName = line1CName;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withLineC2Id(String lineC2Id) {
        this.lineC2Id = lineC2Id;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withLineC2Name(String lineC2Name) {
        this.lineC2Name = lineC2Name;
        return this;
    }
}
