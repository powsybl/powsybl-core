/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.HvdcLineTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class HvdcLineContingency extends AbstractSidedContingency {

    public HvdcLineContingency(String id) {
        super(id);
    }

    public HvdcLineContingency(String id, String voltageLevelId) {
        super(id, voltageLevelId);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.HVDC_LINE;
    }

    @Override
    public Tripping toModification() {
        return new HvdcLineTripping(id, voltageLevelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voltageLevelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HvdcLineContingency other) {
            return id.equals(other.id) && Objects.equals(voltageLevelId, other.voltageLevelId);
        }
        return false;
    }
}
