/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum HistoDbAttr {
   I,
   P,
   Q,
   V,
   IP,
   B,
   PP,
   PN,
   RTC,
   PTC,
   T,
   TOPO,
   TOPOHASH,
   PGEN,
   QGEN,
   PLOAD,
   QLOAD,
   QSHUNT,
   P0, Q0, // only for dangling lines
   VMIN,
   VMAX,
   QR, // reactive reserve
   BC // bus count
}
