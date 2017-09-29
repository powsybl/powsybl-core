/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.List;

/**
 * An equipment that is part of a substation topology.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Connectable<I extends Connectable<I>> extends Identifiable<I> {

    /**
     * Get the connectable type.
     */
    ConnectableType getType();

    List<? extends Terminal> getTerminals();

    /**
     * Remove the equipment.
     */
    void remove();

}
