/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implements management of results handlers.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public abstract class AbstractLoadFlowActionSimulatorResultBuilder<R> implements LoadFlowActionSimulatorResultBuilder<R> {


    private final List<Consumer<R>> handlers = new ArrayList<>();

    private R result;

    protected void setResult(R result) {
        this.result = result;
        handlers.forEach(h -> h.accept(this.result));
    }

    /**
     * The built result.
     */
    @Override
    public R getResult() {
        Objects.requireNonNull(result, "Result is not built yet.");
        return result;
    }

    @Override
    public void addResultHandler(Consumer<R> handler) {
        Objects.requireNonNull(handler);
        handlers.add(handler);
    }

}
