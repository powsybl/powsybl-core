/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Optional;

/*
 @author Bertrand Rix <bertrand.rix at artelys.com>
 */
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
        return switch (side) {
            case ONE -> ThreeSides.ONE;
            case TWO -> ThreeSides.TWO;
        };
    }

    private static ThreeSides toSide(ThreeWindingsTransformer.Side side) {
        return switch (side) {
            case ONE -> ThreeSides.ONE;
            case TWO -> ThreeSides.TWO;
            case THREE -> ThreeSides.THREE;
        };
    }

    public static Terminal getTerminal(Connectable<?> connectable, ThreeSides side) {
        if (connectable instanceof Injection<?> injection) {
            return injection.getTerminal();
        } else if (connectable instanceof Branch<?> branch) {
            return switch (side) {
                case ONE -> branch.getTerminal1();
                case TWO -> branch.getTerminal2();
                case THREE -> throw new IllegalStateException("Unexpected Branch side: " + side.name());
            };
        } else if (connectable instanceof ThreeWindingsTransformer transformer) {
            return switch (side) {
                case ONE -> transformer.getLeg1().getTerminal();
                case TWO -> transformer.getLeg2().getTerminal();
                case THREE -> transformer.getLeg3().getTerminal();
            };
        } else {
            throw new PowsyblException("Unexpected terminal reference identifiable instance: " + connectable.getClass());
        }
    }
}
