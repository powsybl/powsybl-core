/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 6/06/13
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class HarvestDirJob
    extends AbstractDatasourceJob
{

    File harvestedDir;

    public HarvestDirJob(File harvestedDir) throws IOException {
        this.harvestedDir = harvestedDir;
    }

    @Override
    public void run() {
        CimHistoImporter importer = new CimHistoImporter(datasource);

        try {
            importer.addCim(harvestedDir);
            datasource.persistState();
        } catch (IOException e) {
            throw new RuntimeException("Harvest job ended unexpectedly");
        }
    }
}
