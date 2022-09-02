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
public class RevertCreateLineOnLineBuilder {

    private String lineAZId = null;
    private String lineBZId = null;
    private String lineCZId = null;

    private String lineId = null;
    private String lineName = null;

    public RevertCreateLineOnLine build() {
        return new RevertCreateLineOnLine(lineAZId, lineBZId, lineCZId, lineId, lineName);
    }

    public RevertCreateLineOnLineBuilder withLineAZId(String lineAZId) {
        this.lineAZId = lineAZId;
        return this;
    }

    public RevertCreateLineOnLineBuilder withLineBZId(String lineBZId) {
        this.lineBZId = lineBZId;
        return this;
    }

    public RevertCreateLineOnLineBuilder withLineCZId(String lineCZId) {
        this.lineCZId = lineCZId;
        return this;
    }

    public RevertCreateLineOnLineBuilder withLineId(String lineId) {
        this.lineId = lineId;
        return this;
    }

    public RevertCreateLineOnLineBuilder withLineName(String lineName) {
        this.lineName = lineName;
        return this;
    }
}
