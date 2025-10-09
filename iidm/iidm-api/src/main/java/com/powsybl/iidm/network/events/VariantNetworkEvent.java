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
public record VariantNetworkEvent(String sourceVariantId, String targetVariantId, VariantEventType eventType) implements NetworkEvent {

    public enum VariantEventType {
        CREATED,
        OVERWRITTEN,
        REMOVED;
    }

    public VariantNetworkEvent {
        Objects.requireNonNull(sourceVariantId);
        Objects.requireNonNull(eventType);
    }

    @Override
    public Type getType() {
        return Type.VARIANT;
    }
}
