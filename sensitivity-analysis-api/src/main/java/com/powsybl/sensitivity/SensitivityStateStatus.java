/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.commons.json.JsonUtil.compareVersions;
import static com.powsybl.sensitivity.SensitivityAnalysisResult.CONTEXT_NAME;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityStateStatus {

    public record ComponentStatus(int connectedComponentNum, int synchronousComponentNum, LoadFlowResult.ComponentResult.Status status, String statusText) { }

    private final SensitivityState state;

    /**
     * Per-component load flow status (one entry per (connectedComponentNum, synchronousComponentNum) for which a load flow has run).
     */
    private final List<ComponentStatus> componentsLoadFlowStatusList;

    public SensitivityState getState() {
        return state;
    }

    public List<ComponentStatus> getComponentsLoadFlowStatusList() {
        return componentsLoadFlowStatusList;
    }

    public SensitivityStateStatus(SensitivityState state, List<ComponentStatus> statusList) {
        this.state = Objects.requireNonNull(state);
        this.componentsLoadFlowStatusList = ImmutableList.copyOf(Objects.requireNonNull(statusList));
    }
}
