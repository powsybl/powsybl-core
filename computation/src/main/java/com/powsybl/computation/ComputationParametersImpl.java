/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public class ComputationParametersImpl extends AbstractExtendable<ComputationParameters> implements ComputationParameters {

    private final Map<String, Long> timeoutsByCmdId;

    private final Map<String, Long> deadlinesByCmdId;

    ComputationParametersImpl(Map<String, Long> timeoutsByCommandId, Map<String, Long> deadlinesByCommandId) {
        timeoutsByCmdId = Collections.unmodifiableMap(timeoutsByCommandId);
        deadlinesByCmdId = Collections.unmodifiableMap(deadlinesByCommandId);
    }

    @Override
    public OptionalLong getTimeout(String commandId) {
        Objects.requireNonNull(commandId);
        Long t = timeoutsByCmdId.get(commandId);
        if (t == null) {
            return OptionalLong.empty();
        } else {
            return OptionalLong.of(t);
        }
    }

    @Override
    public OptionalLong getDeadline(String commandId) {
        Objects.requireNonNull(commandId);
        Long t = deadlinesByCmdId.get(commandId);
        if (t == null) {
            return OptionalLong.empty();
        } else {
            return OptionalLong.of(t);
        }
    }

}
