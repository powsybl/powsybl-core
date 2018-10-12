/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.simulator.tools.AbstractSecurityAnalysisResultBuilder;
import com.powsybl.security.SecurityAnalysisResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Observer which executes a list of handlers of {@link SecurityAnalysisResult} once the simulation is finished.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisResultHandler extends AbstractSecurityAnalysisResultBuilder {

    private final List<Consumer<SecurityAnalysisResult>> handlers = new ArrayList<>();

    public SecurityAnalysisResultHandler() {
    }

    public SecurityAnalysisResultHandler(Collection<Consumer<SecurityAnalysisResult>> handlers) {
        Objects.requireNonNull(handlers);
        this.handlers.addAll(handlers);
    }

    public SecurityAnalysisResultHandler add(Consumer<SecurityAnalysisResult> handler) {
        handlers.add(handler);
        return this;
    }

    @Override
    public void onFinalStateResult(SecurityAnalysisResult result) {
        handlers.forEach(h -> h.accept(result));
    }
}
