/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.itesla_project.mcla.montecarlo.data.BusData;
import eu.itesla_project.mcla.montecarlo.data.SamplingNetworkData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLStructure;

import eu.itesla_project.mcla.montecarlo.data.GeneratorData;
import eu.itesla_project.mcla.montecarlo.data.LoadData;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MCSMatFileWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MCSMatFileWriter.class);
	
	private Path matFile;
	
	public MCSMatFileWriter(Path matFile) {
		Objects.requireNonNull(matFile, "mat file is null");
		this.matFile = matFile;
	}
	
	public void writeSamplingNetworkData(SamplingNetworkData mcNetworkSamplingData) throws IOException {
		Objects.requireNonNull(mcNetworkSamplingData, "sampling network data is null");
		
		LOGGER.debug("Preparing buses mat data");
		MLStructure buses  = busesDataAsMLStructure(mcNetworkSamplingData.getBusesData());
		LOGGER.debug("Preparing generators mat data");
		MLStructure generators  = generatorsDataAsMLStructure(mcNetworkSamplingData.getGeneratorsData());
		LOGGER.debug("Preparing loads mat data");
		MLStructure loads  = loadsDataAsMLStructure(mcNetworkSamplingData.getLoadsData());
		LOGGER.debug("Saving mat data into " + matFile.toString());
		List<MLArray> mlarray= new ArrayList<>();
		mlarray.add((MLArray) buses );
		mlarray.add((MLArray) generators );
		mlarray.add((MLArray) loads );
		MatFileWriter writer = new MatFileWriter();
        writer.write(matFile.toFile(), mlarray);
	}

	private MLStructure busesDataAsMLStructure(ArrayList<BusData> busesData) {
		MLStructure buses = null;
		buses = new MLStructure("nodo", new int[]{1,busesData.size()});
		int i = 0;
		for (BusData busData : busesData) {
			putBusDataIntoMLStructure(busData, buses, i);
			i++;
		}
		return buses;
	}
	
	private void putBusDataIntoMLStructure(BusData busData, MLStructure buses, int i) {
		LOGGER.debug("Preparing mat data for bus " + busData.getBusId());
		// nome
		MLChar nome = new MLChar("", busData.getBusName());
		buses.setField("nome", nome, 0, i);
		// codice
		MLChar codice = new MLChar("", busData.getBusId());
		buses.setField("codice", codice, 0, i);
		// ID
		MLInt64 id = new MLInt64("", new long[]{ busData.getBusIndex() }, 1);
		buses.setField("ID", id, 0, i);
		// type
		MLInt64 type = new MLInt64("", new long[]{ busData.getBusType() }, 1);
		buses.setField("type", type, 0, i);
		// Vnom
		MLDouble vNom = new MLDouble("", new double[]{ busData.getNominalVoltage() }, 1);
		buses.setField("Vnom", vNom, 0, i);
		// V
		MLDouble v = new MLDouble("", new double[]{ busData.getVoltage() }, 1);
		buses.setField("V", v, 0, i);
		// angle
		MLDouble angle = new MLDouble("", new double[]{ busData.getAngle() }, 1);
		buses.setField("angle", angle, 0, i);
		// Vmin
		MLDouble vMin = new MLDouble("", new double[]{ busData.getMinVoltage() }, 1);
		buses.setField("Vmin", vMin, 0, i);
		// Vmax
		MLDouble vMax = new MLDouble("", new double[]{ busData.getMaxVoltage() }, 1);
		buses.setField("Vmax", vMax, 0, i);
		// P
		MLDouble p = new MLDouble("", new double[]{ busData.getActivePower() }, 1);
		buses.setField("P", p, 0, i);
		// Q
		MLDouble q = new MLDouble("", new double[]{ busData.getReactivePower() }, 1);
		buses.setField("Q", q, 0, i);
	}
	
	private MLStructure generatorsDataAsMLStructure(ArrayList<GeneratorData> generatorsData) {
		MLStructure generators = null;
		generators = new MLStructure("generatore", new int[]{1,generatorsData.size()});
		int i = 0;
		for (GeneratorData generatorData : generatorsData) {
			putGeneratorDataIntoMLStructure(generatorData, generators, i);
			i++;
		}
		return generators;
	}
	
	private void putGeneratorDataIntoMLStructure(GeneratorData generatorData, MLStructure generators, int i) {
		LOGGER.debug("Preparing mat data for generator " + generatorData.getGeneratorId());
		// estremo_ID
		MLInt64 estremo_ID = new MLInt64("", new long[]{ generatorData.getBusIndex() }, 1);
		generators.setField("estremo_ID", estremo_ID, 0, i);
		// estremo
		MLChar estremo = new MLChar("", generatorData.getBusId());
		generators.setField("estremo", estremo, 0, i);
		// codice
		MLChar codice = new MLChar("", generatorData.getGeneratorId());
		generators.setField("codice", codice, 0, i);
		// conn
		int connected = 0;
		if ( generatorData.isConnected() )
			connected = 1;
		MLInt64 conn = new MLInt64("", new long[]{ connected }, 1);
		generators.setField("conn", conn, 0, i);
		// P
		MLDouble p = new MLDouble("", new double[]{ generatorData.getActvePower() }, 1);
		generators.setField("P", p, 0, i);
		// Q
		MLDouble q = new MLDouble("", new double[]{ generatorData.getReactvePower() }, 1);
		generators.setField("Q", q, 0, i);
		// Qmax
		MLDouble qMax = new MLDouble("", new double[]{ generatorData.getMaxReactivePower() }, 1);
		generators.setField("Qmax", qMax, 0, i);
		// Qmin
		MLDouble qMin = new MLDouble("", new double[]{ generatorData.getMinReactivePower() }, 1);
		generators.setField("Qmin", qMin, 0, i);
		// Pmin
		MLDouble pMin = new MLDouble("", new double[]{ generatorData.getMinActivePower() }, 1);
		generators.setField("Pmin", pMin, 0, i);
		// Pmax
		MLDouble pMax = new MLDouble("", new double[]{ generatorData.getMaxActivePower() }, 1);
		generators.setField("Pmax", pMax, 0, i);
		// Anom
		MLDouble aNom = new MLDouble("", new double[]{ generatorData.getNominalPower() }, 1);
		generators.setField("Anom", aNom, 0, i);
		// RES
		MLInt64 res = new MLInt64("", new long[]{ generatorData.getRenewableEnergySource() }, 1);
		generators.setField("RES", res, 0, i);
		// fuel
		MLInt64 fuel = new MLInt64("", new long[]{ generatorData.getFuelType() }, 1);
		generators.setField("fuel", fuel, 0, i);
		// dispacc
		int dispatchable = 0;
		if ( generatorData.isDispatchable() )
			dispatchable = 1;
		MLInt64 dispacc = new MLInt64("", new long[]{ dispatchable }, 1);
		generators.setField("dispacc", dispacc, 0, i);
	}
	
	private MLStructure loadsDataAsMLStructure(ArrayList<LoadData> loadsData) {
		MLStructure loads = null;
		loads = new MLStructure("carico", new int[]{1,loadsData.size()});
		int i = 0;
		for (LoadData loadData : loadsData) {
			putGeneratorDataIntoMLStructure(loadData, loads, i);
			i++;
		}
		return loads;
	}

	private void putGeneratorDataIntoMLStructure(LoadData loadData, MLStructure loads, int i) {
		LOGGER.debug("Preparing mat data for load " + loadData.getLoadId());
		// estremo_ID
		MLInt64 estremo_ID = new MLInt64("", new long[]{ loadData.getBusIndex() }, 1);
		loads.setField("estremo_ID", estremo_ID, 0, i);
		// estremo
		MLChar estremo = new MLChar("", loadData.getBusId());
		loads.setField("estremo", estremo, 0, i);
		// codice
		MLChar codice = new MLChar("", loadData.getLoadId());
		loads.setField("codice", codice, 0, i);
		// conn
		int connected = 0;
		if ( loadData.isConnected() )
			connected = 1;
		MLInt64 disp = new MLInt64("", new long[]{ connected }, 1);
		loads.setField("conn", disp, 0, i);
		// P
		MLDouble p = new MLDouble("", new double[]{ loadData.getActvePower() }, 1);
		loads.setField("P", p, 0, i);
		// Q
		MLDouble q = new MLDouble("", new double[]{ loadData.getReactvePower() }, 1);
		loads.setField("Q", q, 0, i);
		// V
		MLDouble v = new MLDouble("", new double[]{ loadData.getVoltage() }, 1);
		loads.setField("V", v, 0, i);
	}


	
//	public static void writeTestSampledDataMatFile() throws IOException {
//		double[] generatorsActivePower = new double[]{960.01,480.01,0.0};
//		double[] loadsActivePower = new double[]{235.0,235.0,475.0,475.0};
//		double[] loadsReactivePower = new double[]{2.15,2.15,4.55,4.55};
//		MLDouble pGen = new MLDouble("PGEN", new int[]{50,3});
//		MLDouble pLoad = new MLDouble("PLOAD", new int[]{50,4});
//		MLDouble qLoad = new MLDouble("QLOAD", new int[]{50,4});
//		for (int i = 0; i < 50; i++) {
//			for (int j = 0; j < 4; j++) {
//				pLoad.set(loadsActivePower[j], i, j);
//				qLoad.set(loadsReactivePower[j], i, j);
//				loadsActivePower[j] = loadsActivePower[j] + 0.1;
//				loadsReactivePower[j] = loadsReactivePower[j] + 0.01;
//			}
//			for (int j = 0; j < 3; j++) {
//				pGen.set(generatorsActivePower[j], i, j);
//				generatorsActivePower[j] = generatorsActivePower[j] + 0.01;
//			}
//		}
//		MLChar errmsg = new MLChar("errmsg", "Ok");
//		List<MLArray> mlarray= new ArrayList<>();
//		mlarray.add((MLArray) pGen );
//		mlarray.add((MLArray) pLoad );
//		mlarray.add((MLArray) qLoad );
//		mlarray.add((MLArray) errmsg );
//		MatFileWriter writer = new MatFileWriter();
//        writer.write("/itesla_data/MAT/output.mat", mlarray);
//	}
}
