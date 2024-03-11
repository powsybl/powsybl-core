/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Map;

/**
 * SPI for defining dataframes of network extensions.
 *
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
public interface NetworkExtensionDataframeProvider {

    /**
     * The extension name. Should match the IIDM extension name as defined in the extension class.
     */
    String getExtensionName();

    /**
     * the extension information
     */
    ExtensionInformation getExtensionInformation();

    /**
     * The names of dataframes for extensions using multiple dataframes
     */
    List<String> getExtensionTableNames();

    /**
     * Defines the mapping between the network and the extension dataframe.
     */
    Map<String, NetworkDataframeMapper> createMappers();

    void removeExtensions(Network network, List<String> ids);

    default NetworkElementAdder createAdder() {
        return new NetworkElementAdder() {

            @Override
            public List<List<SeriesMetadata>> getMetadata() {
                throw new UnsupportedOperationException("getMetadata method not implemented");
            }

            @Override
            public void addElements(Network network, List<UpdatingDataframe> dataframes) {
                throw new UnsupportedOperationException("addElements method not implemented");
            }

            @Override
            public void addElementsWithBay(Network network, List<UpdatingDataframe> dataframe, boolean throwException,
                                           Reporter reporter) {
                throw new UnsupportedOperationException("addElementsWithBay method not implemented");
            }
        };
    }

}
