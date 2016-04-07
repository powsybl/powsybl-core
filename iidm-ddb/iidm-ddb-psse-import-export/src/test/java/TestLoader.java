/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import eu.itesla_project.iidm.ddb.psse_imp_exp.DdbConfig;
import eu.itesla_project.iidm.ddb.psse_imp_exp.DdbDyrLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class TestLoader {

    static org.slf4j.Logger log = LoggerFactory.getLogger(TestParser.class);

//    @Test
//    @Ignore

    @Ignore("not an unit test")
    @Test
    public void test_00() throws Exception {
        log.info("loads nordic44 dynamic data");
        String psseversion = "32.1";

        DdbDyrLoader dyrLoader = new DdbDyrLoader();
        String dyrFile="/home/itesla/DYRDDB/Nordic44_20140703_1020.DYR";
        String mappingFile="/home/itesla/DYRDDB/mapping_Nordic.csv";

        Path dyrFileUri=Paths.get(dyrFile);
        Path mappingFileUri=Paths.get(mappingFile);

        DdbConfig ddbConfig=DdbConfig.load();
        boolean removeDataAfterHavingLoadedIt=true;
        dyrLoader.load(dyrFileUri, mappingFileUri, psseversion, ddbConfig, removeDataAfterHavingLoadedIt );

    }
}
