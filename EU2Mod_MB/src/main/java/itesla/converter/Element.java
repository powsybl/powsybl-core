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
 * Class representing the iPSL model that corresponds to the Eurostag device.
 * idEu: each block inside the macroblock has an Eurostag id.
 * nameEu: name of the block in Eurostag.
 * pathModelica: path of the iPSL model that represents this block.
 * nameModelica: name of the iPSL model that represents this block.
 * nInputpins: number of the input pins in Modelica.
 * param: name of the parameters in the Modelica model.
 * @author Marc Sabate <sabatem@aia.es>
 * @author Raul Viruez <viruezr@aia.es>
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
