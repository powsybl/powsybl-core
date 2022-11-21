/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * An action activating or deactivating the regulation of a ratio transformer
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class RatioTapChangerRegulationAction extends AbstractTapChangerRegulationAction {

    public static final String NAME = "RATIO_TAP_CHANGER_REGULATION";

    public RatioTapChangerRegulationAction(String id, String transformerId, ThreeWindingsTransformer.Side side, boolean regulating) {
        super(id, transformerId, side, regulating);
    }

    @Override
    public String getType() {
        return NAME;
    }
}
