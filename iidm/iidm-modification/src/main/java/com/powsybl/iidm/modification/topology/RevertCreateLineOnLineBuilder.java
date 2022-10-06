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

    /**
     * @param  lineAZId      The non-null ID of the first line
     */
    public RevertCreateLineOnLineBuilder withLineAZId(String lineAZId) {
        this.lineAZId = lineAZId;
        return this;
    }

    /**
     * @param lineBZId     The non-null ID of the second line
     */
    public RevertCreateLineOnLineBuilder withLineBZId(String lineBZId) {
        this.lineBZId = lineBZId;
        return this;
    }

    /**
     * @param lineCZId     The non-null ID of the third line (connecting tee point to attached voltage level)
     */
    public RevertCreateLineOnLineBuilder withLineCZId(String lineCZId) {
        this.lineCZId = lineCZId;
        return this;
    }

    /**
     * @param lineId       The non-null ID of the new line to be created
     */
    public RevertCreateLineOnLineBuilder withLineId(String lineId) {
        this.lineId = lineId;
        return this;
    }

    /**
     * @param lineName     The optional name of the new line to be created
     */
    public RevertCreateLineOnLineBuilder withLineName(String lineName) {
        this.lineName = lineName;
        return this;
    }
}
