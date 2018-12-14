/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class DefaultNetworkReducerBuilder {

    private NetworkFilter filter;

    private ReductionOptions options = new ReductionOptions();

    private List<NetworkReducerObserver> observers = new ArrayList<>();

    public DefaultNetworkReducerBuilder setNetworkFilter(NetworkFilter filter) {
        this.filter = Objects.requireNonNull(filter);
        return this;
    }

    public DefaultNetworkReducerBuilder setReductionOptions(ReductionOptions options) {
        this.options = Objects.requireNonNull(options);
        return this;
    }

    public DefaultNetworkReducerBuilder setWithDanglingLines(boolean withDanglingLines) {
        options.setWithDanglingLlines(withDanglingLines);
        return this;
    }

    public DefaultNetworkReducerBuilder setObservers(Collection<NetworkReducerObserver> observers) {
        Objects.requireNonNull(observers);
        this.observers.clear();
        this.observers.addAll(observers);
        return this;
    }

    public DefaultNetworkReducerBuilder addObserver(NetworkReducerObserver observer) {
        observers.add(Objects.requireNonNull(observer));
        return this;
    }

    public DefaultNetworkReducer build() {
        return new DefaultNetworkReducer(filter, options, observers);
    }
}
