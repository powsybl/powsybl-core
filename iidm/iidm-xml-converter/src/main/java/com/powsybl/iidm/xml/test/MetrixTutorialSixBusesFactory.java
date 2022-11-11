/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.ImportOptions;
import com.powsybl.iidm.xml.NetworkXml;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class MetrixTutorialSixBusesFactory {

    private MetrixTutorialSixBusesFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        return NetworkXml.read(MetrixTutorialSixBusesFactory.class.getResourceAsStream("/metrix-tutorial-6-buses-network.xiidm"), new ImportOptions(), null, networkFactory);
    }
}
