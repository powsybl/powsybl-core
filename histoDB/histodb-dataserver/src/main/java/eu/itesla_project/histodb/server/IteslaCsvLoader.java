/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.pepito.data.CSVLoader;
import com.csvreader.CsvReader;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 7/03/13
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class IteslaCsvLoader
    extends CSVLoader
{
    public IteslaCsvLoader(File path) {
        super(path);
    }

    /* TODO refactor superclass to allow redefinition of parsing
    @Override
    protected String[] readHeaders(CsvReader data) throws IOException {
        String[] headers = super.readHeaders(data);
        for (int i=0;i<5;i++) data.readRecord(); // skip the iTesla headers

        return headers;
    }
    */
}
