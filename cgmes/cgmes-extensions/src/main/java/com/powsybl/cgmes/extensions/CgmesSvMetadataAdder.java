/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface CgmesSvMetadataAdder extends ExtensionAdder<Network, CgmesSvMetadata> {

    CgmesSvMetadataAdder setDescription(String description);

    CgmesSvMetadataAdder setSvVersion(int svVersion);

    CgmesSvMetadataAdder addDependency(String dependency);

    CgmesSvMetadataAdder setModelingAuthoritySet(String modelingAuthoritySet);

    @Override
    default Class<CgmesSvMetadata> getExtensionClass() {
        return CgmesSvMetadata.class;
    }
}
