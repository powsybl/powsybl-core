/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.Map;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface BaseVoltageMapping extends Extension<Network> {

    String NAME = "baseVoltageMapping";

    interface BaseVoltageSource {

        String getId();

        double getNominalV();

        Source getSource();
    }

    Map<Double, BaseVoltageSource> getBaseVoltages();

    BaseVoltageSource getBaseVoltage(double nominalVoltage);

    boolean isBaseVoltageMapped(double nominalVoltage);

    boolean isBaseVoltageEmpty();

    BaseVoltageMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source);

    Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap();

    @Override
    default String getName() {
        return NAME;
    }
}
