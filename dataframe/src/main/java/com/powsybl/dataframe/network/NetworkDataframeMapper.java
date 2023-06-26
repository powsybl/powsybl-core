/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network;

import com.powsybl.dataframe.DataframeHandler;
import com.powsybl.dataframe.DataframeMapper;
import com.powsybl.iidm.network.Network;

/**
 * Provides methods to map an object's data to/from dataframes.
 * <p>
 * The dataframe data can be read by a {@link DataframeHandler},
 * and provided by variants of "indexed series".
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface NetworkDataframeMapper extends DataframeMapper<Network> {

}
