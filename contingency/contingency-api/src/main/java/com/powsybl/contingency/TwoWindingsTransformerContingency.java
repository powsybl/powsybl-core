/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.AbstractTrippingTask;
import com.powsybl.contingency.tasks.TwoWindingsTransformerTripping;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class TwoWindingsTransformerContingency extends AbstractSidedContingency {

    public TwoWindingsTransformerContingency(String id) {
        super(id);
    }

    public TwoWindingsTransformerContingency(String id, String voltageLevelId) {
        super(id, voltageLevelId);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.TWO_WINDINGS_TRANSFORMER;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new TwoWindingsTransformerTripping(id, voltageLevelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voltageLevelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TwoWindingsTransformerContingency) {
            TwoWindingsTransformerContingency other = (TwoWindingsTransformerContingency) obj;
            return id.equals(other.id) && Objects.equals(voltageLevelId, other.voltageLevelId);
        }
        return false;
    }
}
