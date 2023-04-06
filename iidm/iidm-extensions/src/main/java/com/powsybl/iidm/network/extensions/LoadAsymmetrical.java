/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Load;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadAsymmetrical extends Extension<Load> {

    String NAME = "loadAsymmetrical";

    @Override
    default String getName() {
        return NAME;
    }

    double getDeltaPa();

    LoadAsymmetrical setDeltaPa(double deltaPa);

    double getDeltaPb();

    LoadAsymmetrical setDeltaPb(double deltaPb);

    double getDeltaPc();

    LoadAsymmetrical setDeltaPc(double deltaPc);

    double getDeltaQa();

    LoadAsymmetrical setDeltaQa(double deltaQa);

    double getDeltaQb();

    LoadAsymmetrical setDeltaQb(double deltaQb);

    double getDeltaQc();

    LoadAsymmetrical setDeltaQc(double deltaQc);
}
