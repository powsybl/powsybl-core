/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;

import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
final class MergingViewUtil {

    static Iterable<DanglingLine> getDanglingLines(Iterable<DanglingLine> delegateDanglingLines, MergingViewIndex index) {
        return Iterables.transform(Iterables.filter(delegateDanglingLines, dl -> !index.isMerged(dl)), index::getDanglingLine);
    }

    static Iterable<Line> getLines(Iterable<Line> delegateLines, Iterable<DanglingLine> delegateDl, MergingViewIndex index) {
        return Iterables.concat(Iterables.transform(delegateLines, index::getLine),
                Iterables.transform(Iterables.filter(delegateDl, index::isMerged), dl -> index.getMergedLineByCode(dl.getUcteXnodeCode())));
    }

    static Stream<DanglingLine> getDanglingLineStream(Stream<DanglingLine> delegateDanglingLines, MergingViewIndex index) {
        return delegateDanglingLines
                .filter(dl -> !index.isMerged(dl))
                .map(index::getDanglingLine);
    }

    static Stream<Line> getLineStream(Stream<Line> delegateLines, Stream<DanglingLine> delegateDl, MergingViewIndex index) {
        return Stream.concat(
                delegateLines
                        .map(index::getLine),
                delegateDl
                        .filter(index::isMerged)
                        .map(dl -> index.getMergedLineByCode(dl.getUcteXnodeCode())));
    }

    private MergingViewUtil() {
    }
}
