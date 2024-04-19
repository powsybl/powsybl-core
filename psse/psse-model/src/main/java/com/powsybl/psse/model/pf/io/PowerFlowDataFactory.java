/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class PowerFlowDataFactory {

    private PowerFlowDataFactory() {
    }

    public static PowerFlowData create(String extension) {
        // When only extension is given,
        // We create PowerFlowData objects that are able to read the case identification
        // To determine if the file is valid and get its version
        if (extension.equalsIgnoreCase("rawx")) {
            return new PowerFlowRawxDataAllVersions();
        } else {
            return new PowerFlowRawDataAllVersions();
        }
    }

    public static PowerFlowData create(String extension, PsseVersion version) {
        if (extension.equalsIgnoreCase("rawx")) {
            switch (version.major()) {
                case V35:
                    return new PowerFlowRawxData35();
                default:
                    throw new PsseException("Unsupported version " + version);
            }
        } else {
            switch (version.major()) {
                case V35:
                    return new PowerFlowRawData35();
                case V33:
                    return new PowerFlowRawData33();
                case V32:
                    return new PowerFlowRawData32();
                default:
                    throw new PsseException("Unsupported version " + version);
            }
        }
    }
}
