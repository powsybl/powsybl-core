/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

import eu.itesla_project.sampling.MatlabException;
import eu.itesla_project.mcla.montecarlo.data.SampledData;
import eu.itesla_project.sampling.util.Utils;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MCSMatFileReader {
	
	private Map<String, MLArray> matFileContent;
	
	public MCSMatFileReader(Path matFile) throws Exception {
		Objects.requireNonNull(matFile, "mat file is null");
		MatFileReader sampledDataFileReader = new MatFileReader();
        matFileContent = sampledDataFileReader.read(matFile.toFile());
        String errorMessage = Utils.MLCharToString((MLChar) matFileContent.get("errmsg"));
        if ( !("Ok".equalsIgnoreCase(errorMessage)) ) {
            throw new MatlabException(errorMessage);
        }
	}
	
	public SampledData getSampledData() {
		// get generators active power
        MLDouble pGen = (MLDouble) matFileContent.get("PGEN");
        double[][] generatorsActivePower = null;
        if ( pGen != null )
        	generatorsActivePower = pGen.getArray();
        // get loads active power
        MLDouble pLoad = (MLDouble) matFileContent.get("PLOAD");
        double[][] loadsActivePower = null;
        if ( pLoad != null )
        	loadsActivePower = pLoad.getArray();
        // get loads reactive power
        MLDouble qLoad = (MLDouble) matFileContent.get("QLOAD");
        double[][] loadsReactivePower = null;
        if ( qLoad != null )
        	loadsReactivePower = qLoad.getArray();
        
        SampledData sampledData = new SampledData(generatorsActivePower, loadsActivePower, loadsReactivePower);
        return sampledData;
	}

}
