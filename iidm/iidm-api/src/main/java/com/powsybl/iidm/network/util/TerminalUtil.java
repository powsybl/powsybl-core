package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.Optional;

public final class TerminalUtil {

    private TerminalUtil() {
    }

    public static Optional<ThreeSides> getConnectableSide(Terminal terminal) {
        Connectable<?> c = terminal.getConnectable();
        if (c instanceof Injection) {
            return Optional.empty();
        } else if (c instanceof Branch<?> branch) {
            return Optional.of(toSide(branch.getSide(terminal)));
        } else if (c instanceof ThreeWindingsTransformer transformer) {
            return Optional.of(toSide(transformer.getSide(terminal)));
        } else {
            throw new IllegalStateException("Unexpected Connectable instance: " + c.getClass());
        }
    }

    private static ThreeSides toSide(Branch.Side side) {
        if (side.equals(Branch.Side.ONE)) {
            return ThreeSides.ONE;
        } else {
            return ThreeSides.TWO;
        }
    }

    private static ThreeSides toSide(ThreeWindingsTransformer.Side side) {
        if (side.equals(ThreeWindingsTransformer.Side.ONE)) {
            return ThreeSides.ONE;
        } else if (side.equals(ThreeWindingsTransformer.Side.TWO)) {
            return ThreeSides.TWO;
        } else {
            return ThreeSides.THREE;
        }
    }
}
