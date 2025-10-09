/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class TimeSeriesNames extends DefaultNodeCalcVisitor<Void, Set<String>> {

    public static Set<String> list(NodeCalc nodeCalc) {
        Objects.requireNonNull(nodeCalc);
        Set<String> timeSeriesNames = new TreeSet<>();
        nodeCalc.accept(new TimeSeriesNames(), timeSeriesNames, 0);
        return timeSeriesNames;
    }

    @Override
    public Void visit(TimeSeriesNameNodeCalc nodeCalc, Set<String> timeSeriesNames) {
        timeSeriesNames.add(nodeCalc.getTimeSeriesName());
        return null;
    }
}
