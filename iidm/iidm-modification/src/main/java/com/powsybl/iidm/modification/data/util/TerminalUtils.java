/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.data.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public final class TerminalUtils {

    public static Terminal getTerminal(Network network, String regulatingConnectableId, String regulatingSide) {
        if (regulatingConnectableId == null) {
            return null;
        }
        Connectable<?> c = network.getConnectable(regulatingConnectableId);
        if (c == null) {
            throw new PowsyblException("Given regulating connectable " + regulatingConnectableId + " does not exist");
        }
        if (c instanceof Injection) {
            return ((Injection<?>) c).getTerminal();
        } else if (c instanceof Branch) {
            if (regulatingSide == null) {
                throw new PowsyblException("Undefined side for regulation on branch");
            }
            return ((Branch<?>) c).getTerminal(Branch.Side.valueOf(regulatingSide));
        } else if (c instanceof ThreeWindingsTransformer) {
            if (regulatingSide == null) {
                throw new PowsyblException("Undefined side for regulation on three-windings transformer");
            }
            return ((ThreeWindingsTransformer) c).getTerminal(ThreeWindingsTransformer.Side.valueOf(regulatingSide));
        }
        throw new AssertionError("Unexpected type of connectable " + regulatingConnectableId);
    }

    private TerminalUtils() {
    }
}
