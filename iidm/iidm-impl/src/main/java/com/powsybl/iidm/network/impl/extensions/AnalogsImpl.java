/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Analog;
import com.powsybl.iidm.network.extensions.AnalogAdder;
import com.powsybl.iidm.network.extensions.Analogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class AnalogsImpl<C extends Connectable<C>> extends AbstractExtension<C> implements Analogs<C> {

    private final List<Analog> analogs = new ArrayList<>();

    AnalogsImpl<C> addAnalog(AnalogImpl analog) {
        analogs.add(analog);
        return this;
    }

    @Override
    public Collection<Analog> getAnalogs() {
        return Collections.unmodifiableList(analogs);
    }

    @Override
    public Analog getAnalog(String id) {
        return analogs.stream()
                .filter(a -> a.getId() != null && a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public AnalogAdder newAnalog() {
        return new AnalogAdderImpl(this);
    }
}
