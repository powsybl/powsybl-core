/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class InjectionBasedBusRef implements BusRef {

    protected final Injection injection;

    public InjectionBasedBusRef(Injection terminal) {
        this.injection = Objects.requireNonNull(terminal);
    }

    @Override
    public Optional<Bus> resolve() {
        return Optional.ofNullable(injection.getTerminal().getBusView().getBus());
    }
}
