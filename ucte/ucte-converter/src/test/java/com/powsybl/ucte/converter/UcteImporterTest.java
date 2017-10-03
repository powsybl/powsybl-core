/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import org.junit.Test;
/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class UcteImporterTest {
    @Test
    public void trimIssueTest() throws Exception  {
        // Import network that could fail because of id conflicts due to trim mechanism
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("importIssue.uct", getClass().getResourceAsStream("/importIssue.uct"));
        new UcteImporter().importData(dataSource, null);
    }
}
