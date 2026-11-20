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
public interface DcNodeAdder extends IdentifiableAdder<DcNode, DcNodeAdder> {

    DcNodeAdder setNominalV(double nominalV);

    /**
     * @param lowVoltageLimit low voltage limit in kV, bounding the voltage magnitude.
     * It must be positive, zero or NaN (undefined) and strictly less than highVoltageLimit if they are both defined.
     * @return this adder, for chaining
     */
    DcNodeAdder setLowVoltageLimit(double lowVoltageLimit);

    /**
     * @param highVoltageLimit high voltage limit in kV, bounding the voltage magnitude.
     * It must be strictly positive or NaN (undefined) and strictly greater than lowVoltageLimit if they are both defined.
     * @return this adder, for chaining
     */
    DcNodeAdder setHighVoltageLimit(double highVoltageLimit);

    @Override
    DcNode add();
}
