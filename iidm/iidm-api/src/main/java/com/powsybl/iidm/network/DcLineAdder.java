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
public interface DcLineAdder extends IdentifiableAdder<DcLine, DcLineAdder> {

    /**
     * Set the resistance of the DC line in &#937. The resistance value must be positive.
     * @param r the resistance value of the DC line in &#937, must be positive
     * @return this adder, for chaining
     */
    DcLineAdder setR(double r);

    DcLineAdder setDcNode1(String dcNode1);

    DcLineAdder setConnected1(boolean connected1);

    DcLineAdder setDcNode2(String dcNode2);

    DcLineAdder setConnected2(boolean connected2);

    @Override
    DcLine add();
}
