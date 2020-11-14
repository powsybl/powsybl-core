/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseConstants.PsseVersion;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class RawDataFactory {

    private RawDataFactory() {
    }

    public static RawData create(String extension) {
        if (extension.equalsIgnoreCase("rawx")) {
            return new RawXData35();
        } else {
            return new RawData33();
        }
    }

    public static RawData create(String extension, PsseVersion version) {
        if (extension.equalsIgnoreCase("rawx")) {
            return new RawXData35();
        } else {
            if (version == PsseVersion.VERSION_35) {
                return new RawData35();
            }
            return new RawData33();
        }
    }
}
