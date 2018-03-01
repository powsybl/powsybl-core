/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.io.IOException;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

/**
*
* @author Ferrari Giovanni <giovanni.ferrari@techrain.eu>
*/
public interface AmplExtensionWriter {

    public String getName();

    public void write(Extension<?> ext, Network network, DataSource dataSource, int faultNum, int actionNum,
            boolean append, StringToIntMapper<AmplSubset> mapper, AmplExportConfig config) throws IOException;
}
