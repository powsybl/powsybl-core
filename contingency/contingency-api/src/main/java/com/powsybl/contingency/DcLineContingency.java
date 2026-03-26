/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.DcLineTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

import java.util.Objects;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
public class DcLineContingency extends AbstractSidedContingency {

    public DcLineContingency(String id) {
        super(id);
    }

    public DcLineContingency(String id, String dcNodeId) {
        super(id, dcNodeId);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.DC_LINE;
    }

    @Override
    public Tripping toModification() {
        return new DcLineTripping(id, voltageLevelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voltageLevelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DcLineContingency other) {
            return id.equals(other.id) && Objects.equals(voltageLevelId, other.voltageLevelId);
        }
        return false;
    }
}
