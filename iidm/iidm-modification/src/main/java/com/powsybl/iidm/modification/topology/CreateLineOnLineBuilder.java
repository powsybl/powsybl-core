/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CreateLineOnLineBuilder {

    private String bbsOrBusId = null;

    private Line line = null;
    private LineAdder lineAdder = null;

    private double positionPercent = 50;

    private String fictitiousVlId = null;
    private String fictitiousVlName = null;

    private boolean createFictSubstation = false;
    private String fictitiousSubstationId = null;
    private String fictitiousSubstationName = null;

    private String line1Id = null;
    private String line1Name = null;
    private String line2Id = null;
    private String line2Name = null;

    public CreateLineOnLine build() {
        return new CreateLineOnLine(positionPercent, bbsOrBusId, fictitiousVlId, fictitiousVlName,
                createFictSubstation, fictitiousSubstationId, fictitiousSubstationName,
                line1Id, line1Name, line2Id, line2Name, line, lineAdder);
    }

    public CreateLineOnLineBuilder withPositionPercent(double positionPercent) {
        this.positionPercent = positionPercent;
        return this;
    }

    public CreateLineOnLineBuilder withBusbarSectionOrBusId(String bbsOrBusId) {
        this.bbsOrBusId = bbsOrBusId;
        return this;
    }

    public CreateLineOnLineBuilder withFictitiousVoltageLevelId(String fictitiousVlId) {
        this.fictitiousVlId = fictitiousVlId;
        return this;
    }

    public CreateLineOnLineBuilder withFictitiousVoltageLevelName(String fictitiousVlName) {
        this.fictitiousVlName = fictitiousVlName;
        return this;
    }

    public CreateLineOnLineBuilder withCreateFictitiousSubstation(boolean createFictSubstation) {
        this.createFictSubstation = createFictSubstation;
        return this;
    }

    public CreateLineOnLineBuilder withFictitiousSubstationId(String fictitiousSubstationId) {
        this.fictitiousSubstationId = fictitiousSubstationId;
        return this;
    }

    public CreateLineOnLineBuilder withFictitiousSubstationName(String fictitiousSubstationName) {
        this.fictitiousSubstationName = fictitiousSubstationName;
        return this;
    }

    public CreateLineOnLineBuilder withLine1Id(String line1Id) {
        this.line1Id = line1Id;
        return this;
    }

    public CreateLineOnLineBuilder withLine1Name(String line1Name) {
        this.line1Name = line1Name;
        return this;
    }

    public CreateLineOnLineBuilder withLine2Id(String line2Id) {
        this.line2Id = line2Id;
        return this;
    }

    public CreateLineOnLineBuilder withLine2Name(String line2Name) {
        this.line2Name = line2Name;
        return this;
    }

    public CreateLineOnLineBuilder withLine(Line line) {
        this.line = line;
        if (fictitiousVlId == null) {
            fictitiousVlId = line.getId() + "_VL";
        }
        if (line1Id == null) {
            line1Id = line.getId() + "_1";
        }
        if (line2Id == null) {
            line2Id = line.getId() + "_2";
        }
        if (fictitiousSubstationId == null) {
            fictitiousSubstationId = line.getId() + "_S";
        }
        return this;
    }

    public CreateLineOnLineBuilder withLineAdder(LineAdder lineAdder) {
        this.lineAdder = lineAdder;
        return this;
    }
}
