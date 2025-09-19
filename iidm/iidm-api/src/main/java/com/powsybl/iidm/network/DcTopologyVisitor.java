/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcTopologyVisitor {

    default void visitDcGround(DcGround dcGround) {
        // empty default implementation
    }

    default void visitDcLine(DcLine dcLine, TwoSides side) {
        // empty default implementation
    }

    // TODO update following TerminalNumber PR https://github.com/powsybl/powsybl-core/pull/3533
    default void visitAcDcConverter(AcDcConverter<?> converter, TwoSides side) {
        // empty default implementation
    }

}
