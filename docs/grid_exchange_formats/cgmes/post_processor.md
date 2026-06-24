# CGMES post-processors

## CgmesDLImportPostProcessor
This post-processor loads the diagram layout (DL) profile contained in the CGMES file, if available, into the triplestore.
The diagram layout profile contains the data which is necessary to represent a drawing of the diagram corresponding to the CGMES file.
For instance, it contains the position of all equipment.

This post-processor is enabled by adding the name `cgmesDLImport` to the list associated to `iidm.import.cgmes.post-processors` property.

## CgmesGLImportPostProcessor
This post-processor loads the geographical location (GL) profile contained in the CGMES file, if available. The GL profile holds the geographical coordinates of the network equipment; the post-processor uses them to add the [substation position](../../grid_model/extensions.md#substation-position) and [line position](../../grid_model/extensions.md#line-position) extensions to the network.

This post-processor is enabled by adding the name `cgmesGLImport` to the list associated to the `iidm.import.cgmes.post-processors` property.

## CgmesMeasurementsPostProcessor
This post-processor imports the measurements contained in the CGMES file. The CGMES analog and discrete measurements are converted to the [measurements](../../grid_model/extensions.md#measurements) and [discrete measurements](../../grid_model/extensions.md#discrete-measurements) extensions, attached to the relevant network equipment.

This post-processor is enabled by adding the name `measurements` to the list associated to the `iidm.import.cgmes.post-processors` property.

## CgmesShortCircuitPostProcessor
This post-processor imports the short-circuit data contained in the CGMES file. In particular, the generator short-circuit data is added to the network through the [generator short-circuit](../../grid_model/extensions.md#generator-short-circuit) extension.

This post-processor is enabled by adding the name `shortcircuit` to the list associated to the `iidm.import.cgmes.post-processors` property.

## EntsoeCategoryPostProcessor
This post-processor reads the ENTSO-E category code of the generators from the description of their CGMES `GeneratingUnit`. When the description is a valid positive integer, it is added to the generator through the [generator ENTSO-E category](../../grid_model/extensions.md#generator-entso-e-category) extension.

This post-processor is enabled by adding the name `EntsoeCategory` to the list associated to the `iidm.import.cgmes.post-processors` property.

## PhaseAngleClock
This post-processor reads the `phaseAngleClock` attribute of the two- and three-winding transformers of the CGMES file. When it is non-zero, it is added to the transformer through the [two-winding](../../grid_model/extensions.md#two-winding-transformer-phase-angle-clock) or [three-winding transformer phase angle clock](../../grid_model/extensions.md#three-winding-transformer-phase-angle-clock) extension.

This post-processor is enabled by adding the name `PhaseAngleClock` to the list associated to the `iidm.import.cgmes.post-processors` property.