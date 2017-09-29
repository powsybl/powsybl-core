/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteElementId {

    private final UcteNodeCode nodeCode1;
    private final UcteNodeCode nodeCode2;
    private char orderCode;

    public UcteElementId(UcteNodeCode nodeCode1, UcteNodeCode nodeCode2, char orderCode) {
        this.nodeCode1 = Objects.requireNonNull(nodeCode1);
        this.nodeCode2 = Objects.requireNonNull(nodeCode2);
        this.orderCode = orderCode;
    }

    /**
     * Gets node 1 code.
     * @return node 1 code
     */
    public UcteNodeCode getNodeCode1() {
        return nodeCode1;
    }

    /**
     * Gets node 2 code.
     * @return node 2 code
     */
    public UcteNodeCode getNodeCode2() {
        return nodeCode2;
    }

    /**
     * Gets order code (1, 2, 3 ... 9, A, B, C ... Z).
     * @return order code
     */
    public char getOrderCode() {
        return orderCode;
    }

    /**
     * Sets order code (1, 2, 3 ... 9, A, B, C ... Z).
     * @param orderCode order code
     */
    public void setOrderCode(char orderCode) {
        this.orderCode = orderCode;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(nodeCode1, nodeCode2, orderCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UcteElementId) {
            UcteElementId id = (UcteElementId) obj;
            return id.nodeCode1.equals(nodeCode1)
                    && id.nodeCode2.equals(nodeCode2)
                    && id.orderCode == orderCode;
        }
        return false;
    }

    @Override
    public String toString() {
        return nodeCode1.toString() + " " + nodeCode2.toString() + " " + orderCode;
    }

}
