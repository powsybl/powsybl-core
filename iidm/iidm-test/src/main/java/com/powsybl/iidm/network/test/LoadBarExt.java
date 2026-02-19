/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class LoadBarExt extends AbstractExtension<Load> {
    private Point point;
    private Integer value = null;

    public LoadBarExt(Load load) {
        super(load);
    }

    @Override
    public String getName() {
        return "loadBar";
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public record Point(double x, double y) { }
}
