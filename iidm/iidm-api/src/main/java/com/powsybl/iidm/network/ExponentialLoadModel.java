/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * Exponential load model.
 * p = p0 * (v / v0)^np
 * q = q0 * (v / v0)^nq
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ExponentialLoadModel extends LoadModel {

    @Override
    default LoadModelType getType() {
        return LoadModelType.EXPONENTIAL;
    }

    double getNp();

    ExponentialLoadModel setNp(double np);

    double getNq();

    ExponentialLoadModel setNq(double nq);
}
