/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.data;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Network;

/**
 * This interface is dedicated to {@link NetworkModification} serialization.
 * A {@link com.fasterxml.jackson.databind.ser.std.StdSerializer} is associated with each {@link NetworkModificationData}.
 * Implementations of this interface contain all <b>raw</b> data (no business logic) allowing to create the corresponding {@link NetworkModification},
 * which can be created via {@link NetworkModificationData#toModification(Network)}.
 * Be careful that you cannot serialize a {@link NetworkModification} if you don't use this class hierarchy.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface NetworkModificationData<D extends NetworkModificationData<D, M>, M extends NetworkModification> {

    String getType();

    void copy(D data);

    M toModification(Network network);
}
