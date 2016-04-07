/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionContext {

    private Integer inegNum;
    private Integer convexNum;

    public ExpressionContext() {
    }

    public ExpressionContext(Integer inegNum, Integer convexNum) {
        this.inegNum = inegNum;
        this.convexNum = convexNum;
    }

    public Integer getConvexNum() {
        return convexNum;
    }

    public Integer getInegNum() {
        return inegNum;
    }

}
