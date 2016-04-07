/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DdbConfig;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DdbDtaImpExp;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class TestLoader {

    static org.slf4j.Logger log = LoggerFactory.getLogger(TestLoader.class);


    @Ignore("not an unit test")
    @Test
    public void test_00() throws Exception {
        log.info("loads dynamic data");
        String jbossHost="127.0.0.1";
        String jbossPort="8080";
        String jbossUser="user";
        String jbossPassword="password";
        String dataDir="/home/itesla/DDB";
        String eurostagVersion="5.1.1";
        DdbConfig ddbConfig = new DdbConfig(jbossHost, jbossPort, jbossUser, jbossPassword);
        Path ddData=Paths.get(dataDir);
        Path ddPath =ddData.resolve("gene/hyd/STRASH_3.dd");
        Path genPath=ddData.resolve("reguls");
        Path dicoPath=ddData.resolve("dico.txt");

        DdbDtaImpExp ddbImpExp = new DdbDtaImpExp(new DdbConfig(jbossHost, jbossPort, jbossUser, jbossPassword));
        ddbImpExp.setUpdateFlag(false);
        ddbImpExp.loadEurostagData(ddPath, dicoPath, eurostagVersion, genPath);

    }

}
