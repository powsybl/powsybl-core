/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import java.util.Objects;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public abstract class AbstractContingency implements ContingencyElement {

    protected final String id;

    protected AbstractContingency(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractContingency that) {
            return id.equals(that.getId()) && getType() == that.getType();
        }
        return false;
    }
}
