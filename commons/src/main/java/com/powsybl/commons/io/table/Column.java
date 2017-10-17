/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.CellStyle.HorizontalAlign;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Column {

    private final String name;

    private final CellStyle cellStyle;
    
    public Column(String name, CellStyle cellStyle) {
        this.name = name;
        this.cellStyle = cellStyle;
    }

    public CellStyle getCellStyle() {
		return cellStyle;
	}

	public String getName() {
        return name;
    }

}
