/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import java.util.Objects;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public abstract class AbstractInjectionContingency implements ContingencyElement {

    protected final String id;

    public AbstractInjectionContingency(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractInjectionContingency) {
            AbstractInjectionContingency ic = (AbstractInjectionContingency) obj;
            return id.equals(ic.getId()) && getType() == ic.getType();
        }
        return false;
    }
}
