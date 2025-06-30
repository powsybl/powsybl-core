/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * Record used for {@link Validable} message headers
 * @param type a String describing the type of the equipment
 * @param id either the id of the equipment or of its parent container
 * @param parentContainerTypeIfObjectMissesId The type of the parent container when the id corresponds to the container's one
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public record DefaultMessageHeader(String type, String id, String parentContainerTypeIfObjectMissesId) implements Validable.MessageHeader {

    /**
     * Constructor for the basic case of the object having an id (when being validated)
     * @param type the type of equipment
     * @param id the id of the equipment
     */
    public DefaultMessageHeader(String type, String id) {
        this(type, id, null);
    }

    @Override
    public String toString() {
        return type + (parentContainerTypeIfObjectMissesId != null ? " in " + parentContainerTypeIfObjectMissesId : "")
                + " '" + id + "': ";
    }
}
