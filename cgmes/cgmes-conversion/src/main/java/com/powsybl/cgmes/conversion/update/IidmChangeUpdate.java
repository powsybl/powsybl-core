/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.iidm.network.Identifiable;

import java.util.Objects;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class IidmChangeUpdate extends IidmChange {

    public IidmChangeUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        super(identifiable);
        this.attribute = Objects.requireNonNull(attribute);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getAttribute() {
        return attribute;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return getIdentifiable().getNameOrId() + "." + getAttribute() + " = " + getNewValue();
    }

    private final String attribute;
    private final Object oldValue;
    private final Object newValue;
}
