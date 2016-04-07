/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class NetworkDataExporter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkDataExporter.class);
	
	private static String busesFile = "buses.csv";
	private static String linesFile = "lines.csv";
	private static String tfos2WFile = "tfos2w.csv";
	private static String tfos3WFile = "tfos3w.csv";
	private static String generatorsFile = "generators.csv";
	private static String loadsFile = "loads.csv";
	
	public static void export(NetworkData networkData, Path folder) {
		Objects.requireNonNull(networkData, "network data is null");
		LOGGER.info("Exporting data of network {} to folder {}", networkData.getNetworkId(), folder);
		try {
			exportBuses(networkData.getBusesData(), folder);
			exportLines(networkData.getLinesData(), folder);
			exportTfos2W(networkData.getTfos2WData(), folder);
			exportTfos3W(networkData.getTfos3WData(), folder);
			exportGenerators(networkData.getGeneratorsData(), folder);
			exportLoads(networkData.getLoadsData(), folder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void exportBuses(List<BusData> busesData, Path folder) throws IOException {
		exportEquipments(busesData, BusData.getFields(), folder, busesFile);
	}
	
	private static void exportLines(List<LineData> linesData, Path folder) throws IOException {
		exportEquipments(linesData, LineData.getFields(), folder, linesFile);
	}
	
	private static void exportTfos2W(List<Tfo2WData> tfo2WData, Path folder) throws IOException {
		exportEquipments(tfo2WData, Tfo2WData.getFields(), folder, tfos2WFile);
	}
	
	private static void exportTfos3W(List<Tfo3WData> tfo3WData, Path folder) throws IOException {
		exportEquipments(tfo3WData, Tfo3WData.getFields(), folder, tfos3WFile);
	}
	
	private static void exportGenerators(List<GeneratorData> generatorsData, Path folder) throws IOException {
		exportEquipments(generatorsData, GeneratorData.getFields(), folder, generatorsFile);
	}
	
	private static void exportLoads(List<LoadData> loadsData, Path folder) throws IOException {
		exportEquipments(loadsData, LoadData.getFields(), folder, loadsFile);
	}
	
	private static void exportEquipments(List<? extends EquipmentData> equipments, String[] fields, Path folder, String fileName) throws IOException {
		Path csvFile = getCsvFile(folder, fileName);
		LOGGER.info("Exporting equipment data to file {}", csvFile.toFile());
		FileWriter content = new FileWriter(csvFile.toFile());
		CsvWriter cvsWriter = new CsvWriter(content, ',');
		String[] headers = new String[fields.length];
		int i = 0;
		for(String field : fields)
			headers[i++] = field;
		cvsWriter.writeRecord(headers);
		for(EquipmentData equipmentData : equipments) {
			i = 0;
			String[] values = new String[fields.length];
			for(String field : fields)
				values[i++] = equipmentData.getFieldValue(field);
			cvsWriter.writeRecord(values);
		}
		cvsWriter.flush();
		cvsWriter.close();
	}
	
	private static Path getCsvFile(Path folder, String fileName) {
		if ( folder != null )
			return Paths.get(folder.toString(), fileName);
		return Paths.get(fileName);
	}

}