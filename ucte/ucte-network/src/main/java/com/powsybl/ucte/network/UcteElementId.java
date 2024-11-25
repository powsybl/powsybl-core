/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class UcteElementId implements Comparable<UcteElementId> {

    public static final List<Character> ORDER_CODES = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '-', '.', ' ');

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
        if (!isOrderCode(orderCode)) {
            throw new IllegalArgumentException("Invalid order code: " + orderCode);
        }
        this.orderCode = orderCode;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(nodeCode1, nodeCode2, orderCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UcteElementId id) {
            return this.compareTo(id) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return nodeCode1.toString() + " " + nodeCode2.toString() + " " + orderCode;
    }

    @Override
    public int compareTo(UcteElementId ucteElementId) {
        if (ucteElementId == null) {
            throw new NullPointerException("ucteElementId should not be null");
        }
        return this.toString().compareTo(ucteElementId.toString());
    }

    public static Optional<UcteElementId> parseUcteElementId(String id) {
        UcteElementId elementId = null;
        if (isUcteElementId(id)) {
            UcteNodeCode node1 = UcteNodeCode.parseUcteNodeCode(id.substring(0, 8)).orElseThrow(IllegalStateException::new);
            UcteNodeCode node2 = UcteNodeCode.parseUcteNodeCode(id.substring(9, 17)).orElseThrow(IllegalStateException::new);

            elementId = new UcteElementId(node1, node2, id.charAt(18));
        }
        return Optional.ofNullable(elementId);
    }

    public static boolean isUcteElementId(String id) {
        return id != null &&
                id.length() >= 19 &&
                UcteNodeCode.isUcteNodeId(id.substring(0, 8)) &&
                id.charAt(8) == ' ' &&
                UcteNodeCode.isUcteNodeId(id.substring(9, 17)) &&
                id.charAt(17) == ' ' &&
                isOrderCode(id.charAt(18));
    }

    private static boolean isOrderCode(char orderCode) {
        /*
           Update to match modification on UCTE format
           The new update is available on the ENTSO-E website:
           https://docstore.entsoe.eu/Documents/Publications/SOC/Continental_Europe/150420_quality_of_datasets_and_calculations_3rd_edition.pdf
         */
        return ORDER_CODES.contains(orderCode);
    }
}
