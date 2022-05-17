/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MatpowerExporterTest {

    @Test
    public void test() {
        var network = EurostagTutorialExample1Factory.create();
        MemDataSource dataSource = new MemDataSource();
        new MatpowerExporter().export(network, null, dataSource);
    }
}