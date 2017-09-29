/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.converter;

import eu.itesla_project.iidm.import_.Importers;
import org.junit.Test;

import static org.junit.Assert.*;
/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class UcteImporterTest {
    @Test
    public void trimIssueTest() throws Exception  {
        // Import network taht could fail beacuse of id conflicts due to trim mechanism
        Importers.loadNetwork("importIssue.uct", getClass().getResourceAsStream("/importIssue.uct"));
    }
}
