/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractSidedContingency implements ContingencyElement {

    protected final String id;

    protected final String voltageLevelId;

    public AbstractSidedContingency(String id) {
        this(id, null);
    }

    public AbstractSidedContingency(String id, String voltageLevelId) {
        this.id = Objects.requireNonNull(id);
        this.voltageLevelId = voltageLevelId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }
}
