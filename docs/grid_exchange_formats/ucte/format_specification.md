# Format specification

The [UCTE-DEF](https://cimug.ucaiug.org/Groups/Model%20Exchange/UCTE-format.pdf) (UCTE **D**ata **E**xchange **F**ormat) format is an exchange format specified by the UCTE, for the exchange of grid model among its members. The data refer to load flow and three-phase short-circuit studies and describe the interconnected extra high-voltage network. The data are contained in an unformatted standard US ASCII file. The file is divided into 7 different blocks:
- Comments (C)
- Nodes (N)
- Lines (L)
- Two-winding transformers (T)
- Two-winding transformers regulation (RR)
- Two-winding transformers special description (TT)
- Exchange powers (E)

Each block is introduced by a key line consisting of the two characters `##` and of the character given above in brackets. The end of a block is given by the next key line or the end of the file. The information of each block is written in lines, and the contents are separated by a blank (empty space).

The grid is described in Bus/Branch topology, and only a few types of equipment are supported (nodal injections, AC line, two-winding transformer). Fictitious nodes are located at the electric middle of each tie line. The defined X-nodes are binding for all users.

(ucte-file-name-convention)=
## File name convention
The UCTE-DEF format use the following file name convention: `<yyyymmdd>_<HHMM>_<TY><w>_<cc><v>.uct` with:
- `yyyymmdd`: year, month and day
- `HHMM`: hour and minute
- `TY`: the file type
    - `FO`: Day ahead congestion forecast
    - `2D`: 2-days ahead congestion forecast
    - `SN`: Snapshots
    - `RE`: Reference
    - `LT`: Long-term reference
    - `01`...`23`: Intra-day ahead congestion forecast. The value is the number of hours separating the case date and the generation date.
- `w`: day of the week, starting with 1 for Monday
- `cc`: The ISO country-code for national datasets, `UC` for UCTE-wide merged datasets without X nodes and `UX` for UCTE-wide merged datasets with X nodes
- `v`: version number starting with 0

The specifications of the UCTE-DEF format are available [online](https://cimug.ucaiug.org/Groups/Model%20Exchange/UCTE-format.pdf).

