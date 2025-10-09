/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import java.util.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class DefaultNetworkReducerBuilder {

    private NetworkPredicate predicate;

    private ReductionOptions options = new ReductionOptions();

    private List<NetworkReducerObserver> observers = new ArrayList<>();

    public DefaultNetworkReducerBuilder withNetworkPredicate(NetworkPredicate predicate) {
        this.predicate = Objects.requireNonNull(predicate);
        return this;
    }

    public DefaultNetworkReducerBuilder withReductionOptions(ReductionOptions options) {
        this.options = Objects.requireNonNull(options);
        return this;
    }

    public DefaultNetworkReducerBuilder withDanglingLines(boolean withDanglingLines) {
        options.withDanglingLlines(withDanglingLines);
        return this;
    }

    public DefaultNetworkReducerBuilder withObservers(NetworkReducerObserver... observers) {
        return withObservers(Arrays.asList(observers));
    }

    public DefaultNetworkReducerBuilder withObservers(Collection<NetworkReducerObserver> observers) {
        Objects.requireNonNull(observers);
        this.observers.clear();
        this.observers.addAll(observers);
        return this;
    }

    public DefaultNetworkReducer build() {
        return new DefaultNetworkReducer(predicate, options, observers);
    }
}
