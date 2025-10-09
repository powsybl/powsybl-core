/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesTapChangerAdder {

    CgmesTapChangerAdder setId(String id);

    CgmesTapChangerAdder setCombinedTapChangerId(String combinedTapChangerId);

    CgmesTapChangerAdder setType(String type);

    CgmesTapChangerAdder setHiddenStatus(boolean hidden);

    CgmesTapChangerAdder setStep(int step);

    CgmesTapChangerAdder setControlId(String id);

    CgmesTapChanger add();
}
