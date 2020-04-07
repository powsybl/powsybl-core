/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class LineToACLineSegment extends IidmToCgmes {

    LineToACLineSegment() {

        simpleUpdate("r", "cim:ACLineSegment.r", CgmesSubset.EQUIPMENT);
        simpleUpdate("x", "cim:ACLineSegment.x", CgmesSubset.EQUIPMENT);

        computedValueUpdate("b1", "cim:ACLineSegment.bch", CgmesSubset.EQUIPMENT, this::computeBch);
        computedValueUpdate("b2", "cim:ACLineSegment.bch", CgmesSubset.EQUIPMENT, this::computeBch);
        computedValueUpdate("g1", "cim:ACLineSegment.gch", CgmesSubset.EQUIPMENT, this::computeGch);
        computedValueUpdate("g2", "cim:ACLineSegment.gch", CgmesSubset.EQUIPMENT, this::computeGch);
    }

    public String computeBch(Identifiable id) {
        requireLine(id);
        Line line = (Line) id;
        return Double.toString(line.getB1() + line.getB2());
    }

    public String computeGch(Identifiable id) {
        requireLine(id);
        Line line = (Line) id;
        return Double.toString(line.getG1() + line.getG2());
    }

    private void requireLine(Identifiable id) {
        if (!(id instanceof Line)) {
            throw new ClassCastException("Expected Line, got " + id.getClass().getSimpleName());
        }
    }
}
