/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * todo ?
 *   rename to (concrete) DcBranch and add type enum DC_LINE / DC_SERIES_DEVICE ?
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcLine extends DcConnectable<DcLine> {

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_LINE;
    }

    DcTerminal getDcTerminal1();

    DcTerminal getDcTerminal2();

    double getR();

    void setR(double r);
}
