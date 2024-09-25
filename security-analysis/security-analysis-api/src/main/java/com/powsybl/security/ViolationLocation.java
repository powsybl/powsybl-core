/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public interface ViolationLocation {

    String getId();

    String getVoltageLevelId();

    default Optional<String> getBusId() {
        return Optional.empty();
    }

    default List<String> getBusBarIds() {
        return Collections.emptyList();
    }

}
