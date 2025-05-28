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

    DcLineAdder setR(double r);

    DcLineAdder setDcNode1Id(String dcNode1Id);

    DcLineAdder setConnected1(boolean connected1);

    DcLineAdder setDcNode2Id(String dcNode2Id);

    DcLineAdder setConnected2(boolean connected2);

    @Override
    DcLine add();
}
