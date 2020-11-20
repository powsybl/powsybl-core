/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class RawDataFactory {

    private RawDataFactory() {
    }

    public static RawData create(String extension) {
        // When only extension is given,
        // We create Raw Data objects that are able to read the case identification
        // To determine if the file is valid and get its version
        if (extension.equalsIgnoreCase("rawx")) {
            return new RawXDataCommon();
        } else {
            return new RawDataCommon();
        }
    }

    public static RawData create(String extension, PsseVersion version) {
        if (extension.equalsIgnoreCase("rawx")) {
            return new RawXData35();
        } else {
            switch (version) {
                case VERSION_35:
                    return new RawData35();
                case VERSION_33:
                    return new RawData33();
                default:
                    throw new PsseException("Unsupported version " + version);
            }
        }
    }
}
