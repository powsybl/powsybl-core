/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import java.util.OptionalInt;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesTapChanger {

    String getId();

    String getCombinedTapChangerId();

    String getType();

    boolean isHidden();

    OptionalInt getStep();

    String getControlId();

    default void setControlId(String controlId) {
    }

    default void setType(String type) {
    }
}
