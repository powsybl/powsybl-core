/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.iidm.network.Identifiable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class IidmChange {

    public IidmChange(Identifiable identifiable) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.index = COUNTER.getAndIncrement();
    }

    public Identifiable getIdentifiable() {
        return identifiable;
    }

    public int getIndex() {
        return index;
    }

    private final Identifiable identifiable;
    private final int index;
    private static final AtomicInteger COUNTER = new AtomicInteger();
}
