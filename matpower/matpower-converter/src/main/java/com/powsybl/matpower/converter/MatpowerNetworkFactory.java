/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class MatpowerNetworkFactory {

    private MatpowerNetworkFactory() {
    }

    private static Network create(String baseName, NetworkFactory networkFactory, String ext) {
        return new MatpowerImporter().importData(new ResourceDataSource(baseName, new ResourceSet("/", baseName + ext)), networkFactory, null);
    }

    private static Network create(String baseName, NetworkFactory networkFactory) {
        return create(baseName, networkFactory, ".m");
    }

    public static Network create118(NetworkFactory networkFactory) {
        return create("case118", networkFactory);
    }

    public static Network create118() {
        return create118(NetworkFactory.findDefault());
    }

    public static Network create14(NetworkFactory networkFactory) {
        return create("case14", networkFactory);
    }

    public static Network create14() {
        return create14(NetworkFactory.findDefault());
    }

    public static Network create30(NetworkFactory networkFactory) {
        return create("case30", networkFactory);
    }

    public static Network create30() {
        return create30(NetworkFactory.findDefault());
    }

    public static Network create300(NetworkFactory networkFactory) {
        return create("case300", networkFactory);
    }

    public static Network create300() {
        return create300(NetworkFactory.findDefault());
    }

    public static Network create57(NetworkFactory networkFactory) {
        return create("case57", networkFactory);
    }

    public static Network create57() {
        return create57(NetworkFactory.findDefault());
    }

    public static Network create9(NetworkFactory networkFactory) {
        return create("case9", networkFactory);
    }

    public static Network create9() {
        return create9(NetworkFactory.findDefault());
    }
}
