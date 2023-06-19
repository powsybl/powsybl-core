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
    private String dl1Id;
    private String dl2Id;

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
    public TieLineAdderImpl setDanglingLine1(String dl1Id) {
        this.dl1Id = dl1Id;
        return this;
    }

    @Override
    public TieLineAdderImpl setDanglingLine2(String dl2Id) {
        this.dl2Id = dl2Id;
        return this;
    }

    @Override
    public TieLineImpl add() {
        String id = checkAndGetUniqueId();
        if (dl1Id == null || dl2Id == null) {
            throw new ValidationException(this, "undefined dangling line");
        }
        DanglingLineImpl dl1 = network.getDanglingLine(dl1Id);
        DanglingLineImpl dl2 = network.getDanglingLine(dl2Id);
        if (dl1 == null || dl2 == null) {
            throw new ValidationException(this, dl1Id + " and/or " + dl2Id + " are not dangling lines in the network");
        }
        if (dl1 == dl2) {
            throw new ValidationException(this, "danglingLine1 and danglingLine2 are identical (" + dl1.getId() + ")");
        }
        if (dl1.getTieLine().isPresent() || dl2.getTieLine().isPresent()) {
            throw new ValidationException(this, "danglingLine1 (" + dl1Id + ") and/or danglingLine2 (" + dl2Id + ") already has a tie line");
        }
        if (dl1.getUcteXnodeCode() != null && dl2.getUcteXnodeCode() != null && !Objects.equals(dl1.getUcteXnodeCode(), dl2.getUcteXnodeCode())) {
            throw new ValidationException(this, "ucteXnodeCode is not consistent");
        }

        TieLineImpl line = new TieLineImpl(network.getRef(), id, getName(), isFictitious());
        line.attachDanglingLines(dl1, dl2);
        // invalidate components
        getNetwork().getConnectedComponentsManager().invalidate();
        getNetwork().getSynchronousComponentsManager().invalidate();
        network.getIndex().checkAndAdd(line);
        network.getListeners().notifyCreation(line);
        return line;
    }

}
