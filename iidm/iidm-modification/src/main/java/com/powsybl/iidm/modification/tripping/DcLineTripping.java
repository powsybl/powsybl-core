/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
public class DcLineTripping extends AbstractTripping {

    protected final String dcNodeId;

    private final BiFunction<Network, String, DcLine> supplier;

    public DcLineTripping(String dcLineId) {
        this(dcLineId, null);
    }

    public DcLineTripping(String dcLineId, String dcNodeId) {
        this(dcLineId, dcNodeId, Network::getDcLine);
    }

    protected DcLineTripping(String dcLineId, String dcNodeId, BiFunction<Network, String, DcLine> supplier) {
        super(dcLineId);
        this.dcNodeId = dcNodeId;
        this.supplier = supplier;
    }

    @Override
    public String getName() {
        return "DcLineTripping";
    }

    protected String getDcNodeId() {
        return dcNodeId;
    }

    @Override
    public void traverseDc(Network network, Set<DcTerminal> terminalsToDisconnect, Set<DcTerminal> traversedTerminals) {
        Objects.requireNonNull(network);
        DcLine dcLine = supplier.apply(network, id);
        if (dcLine == null) {
            throw createNotFoundException();
        }
        traverseDoubleSidedEquipment(dcNodeId, dcLine.getDcTerminal1(), dcLine.getDcTerminal2(), terminalsToDisconnect, traversedTerminals, dcLine.getType().name());
    }

    protected PowsyblException createNotFoundException() {
        return new PowsyblException("DcLine '" + id + "' not found");
    }
}
