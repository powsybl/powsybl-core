/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.events;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public record UpdateNetworkEvent(String id, String attribute, String variantId, Object oldValue, Object newValue) implements NetworkEvent {
    public UpdateNetworkEvent {
        Objects.requireNonNull(id);
        Objects.requireNonNull(attribute);
    }

    @Override
    public Type getType() {
        return Type.UPDATE;
    }
}
