/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package itesla.converter;

import java.util.ArrayList;
import java.util.List;

/*
 * Element de la librería PowerSystems. Hace una correspondencia con el elemento de Eurostag.
 * idEu: cada bloque del macrobloque tiene un id de Eurostag asignado.
 * nameEu: nombre del bloque de Eurostag
 * pathModelica: path del correpsondiente bloque dentro de la librería de PowerSystems
 * nameModelica: nombre del bloque dentro de la librería de PowerSystems.
 * nInputpins: numero de input pins del modelo en Modelica
 * param: nombre de los parametros del modelo en Modelica 
 */

public class Element {
	public Integer idEu;
	public String nameEu;
	public String pathModelica;
	public String nameModelica;
	public List<String> param = new ArrayList<String>();
	public double offset;
	public double init_double;
	public Integer nInputPins;
			
	public Element(int idEu, String nameEu, String nameModelica, List<String> param, Integer nInputPins) {
		this.idEu = idEu;
		this.nameEu = nameEu;
		this.pathModelica = nameModelica;
		this.nameModelica = nameModelica.split("\\.")[nameModelica.split("\\.").length-1];
		this.param = param;		
		this.nInputPins = nInputPins;
	}
	
}
