/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_modelica;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Utils {
	
	public static String workingDirectory() {
		return System.getProperty("java.io.tmpdir");
	}
	
	public static String saveFile(String folderName, String fileName, byte[] fileContent) throws IOException {
		Path filePath = Paths.get(folderName, fileName);
		Files.write(filePath, fileContent);
		return filePath.toString();
	}
	
	public static String saveFile1(String folderName, String fileName, byte[] fileContent) throws IOException {
		Path filePath = Paths.get(folderName, fileName);
		OutputStream os = null;
		try {
	         os = new FileOutputStream(new File(filePath.toString()));
	         System.out.println("file name = " + filePath.toString());
	         os.write(fileContent, 0, fileContent.length);
	     } catch (IOException e) {
	         e.printStackTrace();
	     }finally{
	         try {
	             os.close();
	         } catch (IOException e) {
	             e.printStackTrace();
	         }
	     }
		return filePath.toString();
	}
	
	public static byte[] readFile(String folderName, String fileName) throws IOException {
		Path filePath = Paths.get(folderName, fileName);
		byte[] fileContent = Files.readAllBytes(filePath);
		return fileContent;
	}
	
	public static boolean deleteFile(String folderName, String fileName) throws IOException {
		Path filePath = Paths.get(folderName, fileName);
		boolean deleted = Files.deleteIfExists(filePath);
		return deleted;
	}
	
	public static boolean deleteFile(String fileName) throws IOException {
		Path filePath = Paths.get(fileName);
		boolean deleted = Files.deleteIfExists(filePath);
		return deleted;
	}
	
	public static boolean deleteFile1(String fileName) throws IOException {
		File file = new File(fileName);
		boolean deleted = false;
		if ( file.exists() )
			deleted = file.delete();
		return deleted;
	}
	
	public static void throwConverterException(String errorMessage, Logger log) throws ConversionException {
		log.error(errorMessage);
		throw new ConversionException(errorMessage);
	}
	
	public static void checkNull(Object parameter, String parameterName, Logger log) throws ConversionException {
		if ( parameter == null )
			Utils.throwConverterException(parameterName + " is null", log);
	}

}
