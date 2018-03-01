/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.io.IOException;
import java.util.Locale;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

/**
*
* @author Ferrari Giovanni <giovanni.ferrari@techrain.eu>
*/
public interface AmplExtensionWriter {

    static final float INVALID_FLOAT_VALUE = -99999f;
    static final Locale LOCALE = Locale.US;

    public String getName();

    public void write(int extendedNum, Extension<?> ext, Network network, StringToIntMapper<AmplSubset> mapper, DataSource dataSource, boolean append, AmplExportConfig config) throws IOException;
}
