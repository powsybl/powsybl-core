/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.converter;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class IeeeCdfNetworkFactory {

    private IeeeCdfNetworkFactory() {
    }

    private static Network create(String baseName, NetworkFactory networkFactory) {
        return new IeeeCdfImporter().importData(new ResourceDataSource(baseName, new ResourceSet("/", baseName + ".txt")), networkFactory, null);
    }

    public static Network create14(NetworkFactory networkFactory) {
        return create("ieee14cdf", networkFactory);
    }

    public static Network create14() {
        return create14(NetworkFactory.findDefault());
    }

    public static Network create30(NetworkFactory networkFactory) {
        return create("ieee30cdf", networkFactory);
    }

    public static Network create30() {
        return create30(NetworkFactory.findDefault());
    }

    public static Network create57(NetworkFactory networkFactory) {
        return create("ieee57cdf", networkFactory);
    }

    public static Network create57() {
        return create57(NetworkFactory.findDefault());
    }

    public static Network create118(NetworkFactory networkFactory) {
        return create("ieee118cdf", networkFactory);
    }

    public static Network create118() {
        return create118(NetworkFactory.findDefault());
    }

    public static Network create300(NetworkFactory networkFactory) {
        return create("ieee300cdf", networkFactory);
    }

    public static Network create300() {
        return create300(NetworkFactory.findDefault());
    }
}
