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
public record ExtensionRemovalNetworkEvent(String id, String extensionName, boolean after) implements NetworkEvent {
    public ExtensionRemovalNetworkEvent {
        Objects.requireNonNull(id);
        Objects.requireNonNull(extensionName);
    }

    @Override
    public Type getType() {
        return Type.EXTENSION_REMOVAL;
    }
}
