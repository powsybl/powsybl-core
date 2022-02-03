/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.network.modification.tripping.AbstractTripping;
import com.powsybl.network.modification.tripping.LineTripping;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LineContingency extends AbstractSidedContingency {

    public LineContingency(String id) {
        super(id);
    }

    public LineContingency(String id, String voltageLevelId) {
        super(id, voltageLevelId);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.LINE;
    }

    @Override
    public AbstractTripping toTask() {
        return new LineTripping(id, voltageLevelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voltageLevelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineContingency) {
            LineContingency other = (LineContingency) obj;
            return id.equals(other.id) && Objects.equals(voltageLevelId, other.voltageLevelId);
        }
        return false;
    }
}
