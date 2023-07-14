/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class TerminalRef {

    public enum Side {
        ONE,
        TWO,
        THREE
    }

    private final String id;
    private final Side side;

    private TerminalRef(String id, Side side) {
        this.id = id;
        this.side = side;
    }

    public static TerminalRef create(String id) {
        return new TerminalRef(id, Side.ONE);
    }

    public static TerminalRef create(String id, Side side) {
        return new TerminalRef(id, side);
    }

    public static TerminalRef.Side getConnectableSide(Terminal terminal) {
        Connectable<?> c = terminal.getConnectable();
        if (c instanceof Injection) {
            return TerminalRef.Side.ONE;
        } else if (c instanceof Branch) {
            return toSide(((Branch) c).getSide(terminal));
        } else if (c instanceof ThreeWindingsTransformer) {
            return toSide(((ThreeWindingsTransformer) c).getSide(terminal));
        } else {
            throw new IllegalStateException("Unexpected Connectable instance: " + c.getClass());
        }
    }

    // FIXME(Luma) This has been moved from TerminalRefXml,
    // Makes sense to have it here,
    // In the future ...
    // a Terminal ref specified for a switch could be resolved to different terminals depending on the topology
    // This could also be written as TerminalRef.create(id , side).resolve(network)
    public static Terminal resolve(String id, String side, Network network) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Terminal reference identifiable not found: '" + id + "'");
        }
        if (identifiable instanceof Injection) {
            return ((Injection) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return side.equals(Branch.Side.ONE.name()) ? ((Branch) identifiable).getTerminal1()
                    : ((Branch) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else {
            throw new PowsyblException("Unexpected terminal reference identifiable instance: " + identifiable.getClass());
        }
    }

    private static TerminalRef.Side toSide(Branch.Side side) {
        if (side.equals(Branch.Side.ONE)) {
            return TerminalRef.Side.ONE;
        } else {
            return TerminalRef.Side.TWO;
        }
    }

    private static TerminalRef.Side toSide(ThreeWindingsTransformer.Side side) {
        if (side.equals(ThreeWindingsTransformer.Side.ONE)) {
            return TerminalRef.Side.ONE;
        } else if (side.equals(ThreeWindingsTransformer.Side.TWO)) {
            return TerminalRef.Side.TWO;
        } else {
            return TerminalRef.Side.THREE;
        }
    }

    public String getId() {
        return id;
    }

    public Side getSide() {
        return side;
    }
}
