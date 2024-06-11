/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Hugo Marcellin {@literal <hugo.marcelin at rte-france.com>}
 */
public final class InputUtils {
    private InputUtils() {
    }

    public static BOMInputStream toBomInputStream(InputStream inputStream) throws IOException {
        return BOMInputStream.builder().setInputStream(inputStream).setByteOrderMarks(ByteOrderMark.UTF_8).get();
    }
}
