/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ZipLoadModelAdder {

    double SUM_EPSILON = 1E-8; // tolerance on coefficient sum

    ZipLoadModelAdder setC0p(double c0p);

    ZipLoadModelAdder setC1p(double c1p);

    ZipLoadModelAdder setC2p(double c2p);

    ZipLoadModelAdder setC0q(double c0q);

    ZipLoadModelAdder setC1q(double c1q);

    ZipLoadModelAdder setC2q(double c2q);

    LoadAdder add();
}
