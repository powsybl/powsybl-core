/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

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

    private String id;
    private Side side;

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
