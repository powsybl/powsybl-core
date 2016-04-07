/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.test;

import java.io.File;
import java.nio.file.Paths;

import eu.itesla_project.modelica_events_adder.events.ModEventsExport;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaExportEventsTest {
	public static void main(String[] args) {
		
		 if (args == null || args.length == 0) {
			 System.out.println("Modelica exporter eventss requires parameters: " + " moFile eventsFile");
			 System.exit(0);
		 }
		 
		 moFile = Paths.get(args[0]).toFile();
		 eventsFile = Paths.get(args[1]).toFile();
		 
		 ModEventsExport eventsExporter = new ModEventsExport(moFile, eventsFile);
		 eventsExporter.export();
		 
	}
	
	private static File		moFile;
	private static File		eventsFile;
}
