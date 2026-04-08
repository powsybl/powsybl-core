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
public class ReplaceTeePointByVoltageLevelOnLineBuilder {

    private String teePointLine1 = null;
    private String teePointLine2 = null;
    private String teePointLineToRemove = null;
    private String bbsOrBusId = null;
    private String newLine1Id = null;
    private String newLine1Name = null;
    private String newLine2Id = null;
    private String newLine2Name = null;

    public ReplaceTeePointByVoltageLevelOnLine build() {
        return new ReplaceTeePointByVoltageLevelOnLine(teePointLine1, teePointLine2, teePointLineToRemove, bbsOrBusId,
                newLine1Id, newLine1Name, newLine2Id, newLine2Name);
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withTeePointLine1(String teePointLine1) {
        this.teePointLine1 = teePointLine1;
        if (newLine1Id == null) {
            newLine1Id = teePointLine1;
        }
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withTeePointLine2(String teePointLine2) {
        this.teePointLine2 = teePointLine2;
        if (newLine2Id == null) {
            newLine2Id = teePointLine2;
        }
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withTeePointLineToRemove(String teePointLineToRemove) {
        this.teePointLineToRemove = teePointLineToRemove;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withBbsOrBusId(String bbsOrBusId) {
        this.bbsOrBusId = bbsOrBusId;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withNewLine1Id(String newLine1Id) {
        this.newLine1Id = newLine1Id;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withNewLine1Name(String newLine1Name) {
        this.newLine1Name = newLine1Name;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withNewLine2Id(String newLine2Id) {
        this.newLine2Id = newLine2Id;
        return this;
    }

    public ReplaceTeePointByVoltageLevelOnLineBuilder withNewLine2Name(String newLine2Name) {
        this.newLine2Name = newLine2Name;
        return this;
    }
}
