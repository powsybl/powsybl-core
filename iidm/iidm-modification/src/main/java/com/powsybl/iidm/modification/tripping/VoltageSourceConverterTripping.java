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

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
public class VoltageSourceConverterTripping extends AbstractTripping {

    public VoltageSourceConverterTripping(String id) {
        super(id);
    }

    @Override
    public String getName() {
        return "ConverterTripping";
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        AcDcConverter<VoltageSourceConverter> converter = network.getVoltageSourceConverter(id);
        if (converter == null) {
            throw new PowsyblException("Converter '" + id + "' not found");
        }

        for (Terminal t : converter.getTerminals()) {
            TrippingTopologyTraverser.traverse(t, switchesToOpen, terminalsToDisconnect, traversedTerminals);
        }
    }

    @Override
    public void traverseDc(Network network, Set<DcTerminal> terminalsToDisconnect, Set<DcTerminal> traversedDcTerminals) {
        Objects.requireNonNull(network);
        AcDcConverter<VoltageSourceConverter> converter = network.getVoltageSourceConverter(id);
        if (converter == null) {
            throw new PowsyblException("Converter '" + id + "' not found");
        }

        for (DcTerminal t : converter.getDcTerminals()) {
            TrippingTopologyTraverser.traverse(t, terminalsToDisconnect, traversedDcTerminals);
        }
    }
}
