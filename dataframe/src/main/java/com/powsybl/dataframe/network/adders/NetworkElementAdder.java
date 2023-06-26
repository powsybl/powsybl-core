/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Network;

import java.util.List;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public interface NetworkElementAdder {

    /**
     * Get the list of metadata: one list of columns metadata for each input dataframe.
     */
    List<List<SeriesMetadata>> getMetadata();

    /**
     * Adds elements to the network, based on a list of dataframes.
     * The first dataframe is considered the "primary" dataframe, other dataframes
     * can provide additional data (think steps for the tap changers).
     */
    void addElements(Network network, List<UpdatingDataframe> dataframes);

    default void addElementsWithBay(Network network, List<UpdatingDataframe> dataframe, boolean throwException,
                                    Reporter reporter) {
        throw new UnsupportedOperationException();
    }
}
