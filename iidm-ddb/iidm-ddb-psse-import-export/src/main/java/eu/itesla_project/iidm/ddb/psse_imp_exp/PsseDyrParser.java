/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.psse_imp_exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class PsseDyrParser {

    static Logger log = LoggerFactory.getLogger(PsseDyrParser.class);

    public static List<PsseRegister> parseFile(File file) throws IOException {
        List<PsseRegister> list = new ArrayList<>();
        int lastLineRead=0;
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file)))  {
            lineNumberReader.setLineNumber(0);
            String line = null;
            int innerCount=0;
            List<String> aList=new ArrayList<>();
            while ((line = lineNumberReader.readLine()) != null)
            {
                lastLineRead= lineNumberReader.getLineNumber();
				
				// Ignore lines beginning with "/"
				// It is an official (although undocumented) way of putting comments in PSS/E files
				// Found "/ PORTUGAL" line as a comment in a REN Case
				// REN confirmed that it was provided as a solution by PSS/E to some of their requests
				if (line.startsWith("/")) continue;
				
                Matcher m = Pattern.compile("([^\']\\S*|\'.+?\')\\s*").matcher(line);
                while (m.find()) {
                    String entry=m.group(1);
                    if (!("".equals(entry.trim()))) {
                        if (entry.endsWith("/")) {
                            if (entry.length()>1) {
                                aList.add(entry.substring(0,entry.length()-1));
                            }
                            List<Float> parameters= new ArrayList<>();
                            for (int count =3;  count < aList.size(); count=count+1) {
                                    Float parValue=Float.parseFloat(aList.get(count));
                                    parameters.add(parValue);
                            }
                            // The PsseRegister will check model's params against dictionary
							String busNum = aList.get(0);
							// Remove single quotes from model name
							String modelName = aList.get(1).replaceAll("'", "");
							// Remove single quotes from equipment id
							String id = aList.get(2).replaceAll("'","");
                            PsseRegister psseReg=PsseRegisterFactory.createRegister(busNum, modelName, id, parameters);
                            if (psseReg!=null) {
                                list.add(psseReg);
                            }
                            aList=new ArrayList<>();
                            innerCount=0;
                        } else  {
                            aList.add(entry);
                            innerCount = innerCount + 1;
                        }
                    }
                }
            }
        }
        return list;
    }

}
