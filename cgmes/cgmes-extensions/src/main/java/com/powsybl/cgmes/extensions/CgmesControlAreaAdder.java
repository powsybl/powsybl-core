/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesControlAreaAdder {

    CgmesControlAreaAdder setId(String id);

    CgmesControlAreaAdder setName(String name);

    CgmesControlAreaAdder setEnergyIdentificationCodeEic(String energyIdentificationCodeEic);

    CgmesControlAreaAdder setNetInterchange(double netInterchange);

    default CgmesControlAreaAdder setPTolerance(double pTolerance) {
        throw new PowsyblException("Unsupported method");
    }

    CgmesControlArea add();
}
