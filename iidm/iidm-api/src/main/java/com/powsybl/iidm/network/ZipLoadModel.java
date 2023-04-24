/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * ZIP load model.
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

    double getPp();

    ZipLoadModel setPp(double pp);

    double getIp();

    ZipLoadModel setIp(double ip);

    double getZp();

    ZipLoadModel setZp(double zp);

    double getPq();

    ZipLoadModel setPq(double pq);

    double getIq();

    ZipLoadModel setIq(double iq);

    double getZq();

    ZipLoadModel setZq(double zq);
}
