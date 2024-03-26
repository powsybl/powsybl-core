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
public class RevertCreateLineOnLineBuilder {

    private String lineToBeMerged1Id = null;
    private String lineToBeMerged2Id = null;
    private String lineToBeDeletedId = null;

    private String mergedLineId = null;
    private String mergedLineName = null;

    public RevertCreateLineOnLine build() {
        return new RevertCreateLineOnLine(lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId, mergedLineId, mergedLineName);
    }

    /**
     * @param  lineToBeMerged1Id      The non-null ID of the first line
     */
    public RevertCreateLineOnLineBuilder withLineToBeMerged1Id(String lineToBeMerged1Id) {
        this.lineToBeMerged1Id = lineToBeMerged1Id;
        return this;
    }

    /**
     * @param lineToBeMerged2Id     The non-null ID of the second line
     */
    public RevertCreateLineOnLineBuilder withLineToBeMerged2Id(String lineToBeMerged2Id) {
        this.lineToBeMerged2Id = lineToBeMerged2Id;
        return this;
    }

    /**
     * @param lineToBeDeletedId     The non-null ID of the third line (connecting tee point to tapped voltage level)
     */
    public RevertCreateLineOnLineBuilder withLineToBeDeletedId(String lineToBeDeletedId) {
        this.lineToBeDeletedId = lineToBeDeletedId;
        return this;
    }

    /**
     * @param mergedLineId       The non-null ID of the new line to be created
     */
    public RevertCreateLineOnLineBuilder withMergedLineId(String mergedLineId) {
        this.mergedLineId = mergedLineId;
        return this;
    }

    /**
     * @param mergedLineName     The optional name of the new line to be created
     */
    public RevertCreateLineOnLineBuilder withMergedLineName(String mergedLineName) {
        this.mergedLineName = mergedLineName;
        return this;
    }
}
