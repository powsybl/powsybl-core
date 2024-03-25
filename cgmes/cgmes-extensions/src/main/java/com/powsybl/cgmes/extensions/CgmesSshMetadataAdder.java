/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public interface CgmesSshMetadataAdder extends ExtensionAdder<Network, CgmesSshMetadata> {

    default CgmesSshMetadataAdder setId(String id) {
        throw new PowsyblException("Unsupported method");
    }

    CgmesSshMetadataAdder setDescription(String description);

    CgmesSshMetadataAdder setSshVersion(int sshVersion);

    CgmesSshMetadataAdder addDependency(String dependency);

    CgmesSshMetadataAdder setModelingAuthoritySet(String modelingAuthoritySet);

    @Override
    default Class<CgmesSshMetadata> getExtensionClass() {
        return CgmesSshMetadata.class;
    }
}
