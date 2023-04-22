/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ZipLoadModelAdder {

    ZipLoadModelAdder setPp(double pp);

    ZipLoadModelAdder setIp(double ip);

    ZipLoadModelAdder setZp(double zp);

    ZipLoadModelAdder setPq(double pq);

    ZipLoadModelAdder setIq(double iq);

    ZipLoadModelAdder setZq(double zq);

    LoadAdder add();
}
