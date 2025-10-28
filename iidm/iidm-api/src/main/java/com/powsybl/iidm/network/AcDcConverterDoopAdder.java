/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
public interface AcDcConverterDoopAdder {

    AcDcConverterDoopAdder setUMax(double uMax);

    AcDcConverterDoopAdder setUMin(double uMin);

    AcDcConverterDoopAdder setDroopCoefficient(double k);

    AcDcConverterDroop add();
}
