/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * todo:
 *   V / Q control at Pcc
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcVoltageSourceConverter extends DcConverter<DcVoltageSourceConverter>, ReactiveLimitsHolder {

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER;
    }
}
