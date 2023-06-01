/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineAdderImpl extends AbstractIdentifiableAdder<TieLineAdderImpl> implements TieLineAdder {

    private final NetworkImpl network;
    private String bl1Id;
    private String bl2Id;

    TieLineAdderImpl(NetworkImpl network) {
        this.network = network;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return network;
    }

    @Override
    protected String getTypeDescription() {
        return "AC tie Line";
    }

    @Override
    public TieLineAdderImpl setBoundaryLine1(String bl1Id) {
        this.bl1Id = bl1Id;
        return this;
    }

    @Override
    public TieLineAdderImpl setBoundaryLine2(String bl2Id) {
        this.bl2Id = bl2Id;
        return this;
    }

    @Override
    public TieLineImpl add() {
        String id = checkAndGetUniqueId();
        if (bl1Id == null || bl2Id == null) {
            throw new ValidationException(this, "undefined boundary line");
        }
        BoundaryLineImpl bl1 = network.getBoundaryLine(bl1Id);
        BoundaryLineImpl bl2 = network.getBoundaryLine(bl2Id);
        if (bl1 == null || bl2 == null) {
            throw new ValidationException(this, bl1Id + " and/or " + bl2Id + " are not boundary lines in the network");
        }
        if (bl1 == bl2) {
            throw new ValidationException(this, "boundaryLine1 and boundaryLine2 are identical (" + bl1.getId() + ")");
        }
        if (bl1.getTieLine().isPresent() || bl2.getTieLine().isPresent()) {
            throw new ValidationException(this, "boundaryLine1 (" + bl1Id + ") and/or boundaryLine2 (" + bl2Id + ") already has a tie line");
        }
        if (bl1.getUcteXnodeCode() != null && bl2.getUcteXnodeCode() != null && !Objects.equals(bl1.getUcteXnodeCode(), bl2.getUcteXnodeCode())) {
            throw new ValidationException(this, "ucteXnodeCode is not consistent");
        }

        TieLineImpl line = new TieLineImpl(network.getRef(), id, getName(), isFictitious());
        line.attachBoundaryLines(bl1, bl2);
        network.getIndex().checkAndAdd(line);
        network.getListeners().notifyCreation(line);
        return line;
    }

}
