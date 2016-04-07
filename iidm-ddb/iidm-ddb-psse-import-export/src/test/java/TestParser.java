/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import eu.itesla_project.iidm.ddb.psse_imp_exp.PsseDyrParser;
import eu.itesla_project.iidm.ddb.psse_imp_exp.PsseRegister;
import org.junit.Test;
import org.junit.Ignore;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class TestParser {

    static org.slf4j.Logger log = LoggerFactory.getLogger(TestParser.class);

    @Ignore("not an unit test")
    @Test
    public void test_00() throws Exception {
        log.info("test parsing dyr");
        List<PsseRegister> aList= null;
        try {
            aList = PsseDyrParser.parseFile(new File("/home/itesla/DYRDDB/testparser.dyr"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (PsseRegister psseRecord:aList) {
            log.info(psseRecord.toString());
        }



    }
}
