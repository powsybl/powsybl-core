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
 * p = p0 * (c0p + c1p * (v / v0) + c2p * (v / v0)^2)
 * q = q0 * (c0q + c1q * (v / v0) + c2q * (v / v0)^2)
 * with v0 the nominal voltage.
 * Sum of C0p, C1p and C2p must be equal to 1.
 * Sum of C0q, C1q and C2q must be equal to 1.
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
