/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import com.csvreader.CsvReader;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

import eu.itesla_project.modules.mcla.ForecastErrorsStatistics;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Utils {

	private static float[] getFloats(String[] strings) {
		float[] floats = new float[strings.length];
		for (int i = 0; i < strings.length; i++) {
			floats[i] = Float.parseFloat(strings[i]);
		}
		return floats;
	}

	public static ForecastErrorsStatistics readStatisticsFromFile(Path statsFilePath) throws IOException {
		Objects.requireNonNull(statsFilePath);

		CsvReader csvReader = null;
		try {
			csvReader = new CsvReader(statsFilePath.toFile().toString());
			csvReader.readHeaders();
			String[] injectionsIds = csvReader.getHeaders();
			csvReader.readRecord();
			float[] means = getFloats(csvReader.getValues());
			csvReader.readRecord();
			float[] standardDeviations = getFloats(csvReader.getValues());
			return new ForecastErrorsStatistics(injectionsIds, means, standardDeviations);
		} catch (IOException e) {
			throw e;
		} finally {
			if ( csvReader != null )
				csvReader.close();
		}

	}

	public static String[] readStringsArrayFromMat(Path matFile, String stringsArrayName) throws IOException {
		Objects.requireNonNull(matFile, "mat file is null");
		Objects.requireNonNull(stringsArrayName, "strings array name is null");
		MatFileReader matFileReader = new MatFileReader();
		Map<String, MLArray> matFileContent = matFileReader.read(matFile.toFile());
		MLCell stringsArray = (MLCell) matFileContent.get(stringsArrayName);
		String[] strings = new String[stringsArray.getN()];
		for (int i = 0; i < stringsArray.getN(); i++) {
			strings[i] = ((MLChar) stringsArray.get(0, i)).getString(0);
		}
		return strings;
	}
	
	public static double[][] readDoublesMatrixFromMat(Path matFile, String doublesMatrixName) throws IOException {
		Objects.requireNonNull(matFile, "mat file is null");
		MatFileReader matFileReader = new MatFileReader();
		Map<String, MLArray> matFileContent = matFileReader.read(matFile.toFile());
		MLDouble doublesMatrix = (MLDouble) matFileContent.get(doublesMatrixName);
		double[][] doubles = null;
		if ( doublesMatrix != null )
			doubles = doublesMatrix.getArray();
		return doubles;
	}

}
