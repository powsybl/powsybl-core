/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.contingency;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.contingency.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Contingencies and actions database based on modelica events CSV file.
 * <p>Example:
 * <pre>
 *#contingency id;event id;type;device;further parameters
 *0;1;LINE_FAULT;_f1769a39-9aeb-11e5-91da-b8763fd99c5f;Rfault=0.5;Xfault=0.5;k=0.5;time_1=5;time_2=5.3
 * </pre>
 *
 */
/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class CsvFileModelicaContingenciesAndActionsDatabaseClient implements ContingenciesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvFileModelicaContingenciesAndActionsDatabaseClient.class);

    private final Path file;

    public CsvFileModelicaContingenciesAndActionsDatabaseClient(Path file) {
        this.file = file;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        List<Contingency> contingencies = new ArrayList<>();

        try {

            Map<String,Contingency> contingenciesMap= new HashMap<>();
            try (BufferedReader r = Files.newBufferedReader(file, Charset.defaultCharset())) {
                String txt;
                while ((txt = r.readLine()) != null) {
                    if (txt.startsWith("#")) { // comment
                        continue;
                    }
                    if (txt.trim().isEmpty()) {
                        continue;
                    }

                    List<String> entryTokens= Splitter.on(';').omitEmptyStrings().trimResults().splitToList(txt);
                    String contingencyId = entryTokens.get(0);
                    String contingencyElementId = entryTokens.get(1);
                    String contingencyType = entryTokens.get(2);
                    String deviceId = entryTokens.get(3);
                    String deviceId2 = null;

                    String  eventParams = null;
                    //TODO fix this bad handling of a case with no name=value param
                    if ("BREAKER".equals(contingencyType)) {
                        deviceId2=entryTokens.get(4);
                        eventParams = Splitter.on(';').omitEmptyStrings().trimResults().limit(6).splitToList(txt).get(5);
                    } else {
                        deviceId2=null;
                        eventParams = Splitter.on(';').omitEmptyStrings().trimResults().limit(5).splitToList(txt).get(4);
                    }

                    //now split the key=value entries
                    Map<String, String> splitKeyValues = Splitter.on(";")
                            .omitEmptyStrings()
                            .trimResults()
                            .withKeyValueSeparator(Splitter.on('='))
                            .split(eventParams);


                    ContingencyElement newElement=null;
                    switch (contingencyType) {
                        case "BUS_FAULT" : newElement=new MoBusFaultContingency(deviceId,txt,splitKeyValues); break; //1
                        case "LINE_FAULT" : newElement=new MoLineFaultContingency(deviceId,txt,splitKeyValues); break; //2
                        case "LINE_OPEN_REC" : newElement=new MoLineOpenRecContingency(deviceId,txt,splitKeyValues); break; //3
                        case "LINE_2_OPEN" : newElement=new MoLine2OpenContingency(deviceId,txt,splitKeyValues); break; //4
                        case "BANK_MODIF" : newElement=new MoBankModifContingency(deviceId,txt,splitKeyValues); break; //5
                        case "LOAD_MODIF" : newElement=new MoLoadModifContingency(deviceId,txt,splitKeyValues); break; //6
                        case "BREAKER" : newElement=new MoBreakerContingency(deviceId,txt,splitKeyValues); break; //7
                        case "SETPOINT_MODIF" : newElement=new MoSetPointModifContingency(deviceId,txt,splitKeyValues); break; //8  tobevalidated; ref AIA's document
                        default: LOGGER.warn("Contingency type '{}' not handled", contingencyType);
                    }
                    if (newElement == null) {
                        LOGGER.warn("Skip empty contingency " + contingencyId);
                        continue;
                    }

                    Contingency contingency=contingenciesMap.get(contingencyId);
                    if ( contingency == null ) {
                        List<ContingencyElement> elements = new ArrayList<>();
                        elements.add(newElement);
                        contingency=new ContingencyImpl(contingencyId, elements);
                        contingenciesMap.put(contingencyId, contingency);
                    } else {
                        contingency.getElements().add(newElement);
                    }
                }
            }
            contingencies = Lists.newArrayList(contingenciesMap.values());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contingencies;
    }

}
