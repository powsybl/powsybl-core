/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaAdaptersMatParamsWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DymolaAdaptersMatParamsWriter.class);
    private static final String OVERLOAD = "overload";
    private static final String UNDEROVERVOLTAGE = "underovervoltage";
    private static final String SMALLSIGNAL = "smallsignal";
    private static final String TRANSIENT = "transient";

    private HierarchicalINIConfiguration configuration;

    public DymolaAdaptersMatParamsWriter(HierarchicalINIConfiguration configuration)  {
        if (configuration == null) {
            throw new RuntimeException("null config");
        }
        this.configuration=configuration;
        //this below will simply log parameters ..
        for (String section : configuration.getSections()) {
            SubnodeConfiguration node = configuration.getSection(section);
            List<String> paramsSummary = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(node.getKeys(),
                            Spliterator.ORDERED), false).map(p -> p + "=" + node.getString(p)).collect(Collectors.<String>toList());
            LOGGER.info("index {}: {}", section, paramsSummary);
        }
    }

    List<MLArray> overloadMLArray(Double p, Double d){
        MLDouble mP = new MLDouble("p", new double[]{p}, 1);
        MLDouble mD = new MLDouble("d", new double[]{d}, 1);

        List<MLArray> mlarray = Arrays.asList(mP, mD);
        return mlarray;
    }

    public List<MLArray> underovervoltageMLArray(Double p, Double d){
        MLDouble mP = new MLDouble("p", new double[]{p}, 1);
        MLDouble mD = new MLDouble("d", new double[]{d}, 1);

        List<MLArray> mlarray = Arrays.asList(mP, mD);
        return mlarray;
    }

    public List<MLArray> smallsignalMLArray(Double step_min, Double var_min, Double f1, Double f2, Double d1, Double d2, Double d3, Double nm, Double fInstant, Double fDuration) {
        MLDouble mStepMin = new MLDouble("step_min", new double[]{step_min}, 1);
        MLDouble mVarMin = new MLDouble("var_min", new double[]{var_min}, 1);
        MLDouble mF = new MLDouble("f", new double[]{f1, f2}, 1);
        MLDouble mD = new MLDouble("d", new double[]{d1, d2, d3}, 1);
        MLDouble mNm = new MLDouble("Nm", new double[]{nm}, 1);
        MLDouble mFInstant = new MLDouble("f_instant", new double[]{ fInstant }, 1);
        MLDouble mFDuration = new MLDouble("f_duration", new double[]{ fDuration }, 1);

        List<MLArray> mlarray = Arrays.asList(mStepMin, mVarMin, mF, mD, mNm, mFInstant, mFDuration);
        return mlarray;
    }

    public List<MLArray> transientMLArray() {
        //currently, this index does not require any parameters ..
        //let's create a DUMMY one
        MLDouble mP = new MLDouble("DUMMY", new double[]{ 0.0 }, 1);

        List<MLArray> mlarray = Arrays.asList(mP);
        return mlarray;
    }

    private void writeMLArrayToPath(Path fPath, List<MLArray> mlarray) throws IOException {
        try (OutputStream w = Files.newOutputStream(fPath)) {
            //using Writable channel to make it work both for 'standard' and shrinkwrap based filesystems
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            WritableByteChannel wbc= Channels.newChannel(baos);
            new MatFileWriter(wbc,mlarray);
            w.write(baos.toByteArray());
        }
    }

    public void write(String indexName, Path outputMatFile) {
        LOGGER.info("writing input parameters for index '{}' to file  {}", indexName, outputMatFile);
        List<MLArray> mlarray=null;
        switch (indexName) {
            case OVERLOAD: {
                SubnodeConfiguration sc = configuration.getSection(OVERLOAD);
                mlarray=overloadMLArray(sc.getDouble("p"), sc.getDouble("d"));
                break;
            }
            case UNDEROVERVOLTAGE: {
                SubnodeConfiguration sc = configuration.getSection(UNDEROVERVOLTAGE);
                mlarray=underovervoltageMLArray(sc.getDouble("p"), sc.getDouble("d"));
                break;
            }
            case SMALLSIGNAL: {
                SubnodeConfiguration sc = configuration.getSection(SMALLSIGNAL);
                mlarray=smallsignalMLArray(sc.getDouble("step_min"), sc.getDouble("var_min"), sc.getDouble("f_1"), sc.getDouble("f_2"), sc.getDouble("d_1"), sc.getDouble("d_2"), sc.getDouble("d_3"), sc.getDouble("nm"), sc.getDouble("f_instant"), sc.getDouble("f_duration"));
                break;
            }
            case TRANSIENT: {
                SubnodeConfiguration sc = configuration.getSection(TRANSIENT);
                mlarray=transientMLArray();
                break;
            }
            default:
                throw new RuntimeException("index " + indexName + " not handled");
        }
        try {
            writeMLArrayToPath(outputMatFile, mlarray);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}
