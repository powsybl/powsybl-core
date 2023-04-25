/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * ZIP (polynomial) load model.
 * p = p0 * (pp + ip * (v / v0) + zp * (v / v0)^2)
 * q = q0 * (pq + iq * (v / v0) + zq * (v / v0)^2)
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ZipLoadModel extends LoadModel {

    @Override
    default LoadModelType getType() {
        return LoadModelType.ZIP;
    }

    double getC0p();

    ZipLoadModel setC0p(double c0p);

    double getC1p();

    ZipLoadModel setC1p(double c1p);

    double getC2p();

    ZipLoadModel setC2p(double c2p);

    double getC0q();

    ZipLoadModel setC0q(double c0q);

    double getC1q();

    ZipLoadModel setC1q(double c1q);

    double getC2q();

    ZipLoadModel setC2q(double c2q);
}
