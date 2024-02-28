/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface Report {

    /**
     * Get the key of current node.
     * Note that each key needs to correspond to a unique message template.
     * This is required in serialization, in particular due to multilingual support.
     * @return the key
     */
    String getKey();

    /**
     * Get the message of current node, replacing references in the message template with the corresponding values
     * @return the message
     */
    String getMessage();

    /**
     * Get the value corresponding to the given key
     *
     * @param valueKey the key to request
     * @return the value
     */
    Optional<TypedValue> getValue(String valueKey);
}
