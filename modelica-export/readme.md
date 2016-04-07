For the conversion to Modelica follow the following steps:

1. Updload the system (from Eurostag or PSS/E) to the DDB using the itools script (example for PSS/E):

The script must be called from the directory $ITESLA_HOME/platform/iidm-ddb/iidm-ddb-<psse_or_eurostag>-import-export.

./itools ddb-load-psse --dyr-file "<path_to_dyr>\N44_BC.DYR" --mapping-file "<path_to_mapping>\mapping.csv"  --host 127.0.0.1 --port 8080 --user user --password password --psse-version 32.1 --remove-data-flag false

The files you need are:

 * Source engine file (.dta or .dyr files)
 
 * The mapping file: mapping between machines in CIM and PSS/E or Eurostag
 
 * iPSL library

2. Upload the iPSL library to the DDB using the script ddbmo.sh, that must be called from the directory $ITESLA_HOME/platform/iidm-ddb/iidm-ddb-modelica-import-export. The files you need are:
 
 * iPSL library
 
 * The mapping file: mapping between PSS/E or Eurostag and Modelica models.

3. Finally, to do the conversion use the script cim2mo.sh, that must be called from $ITESLA_HOME/platform/modelica-export. There are two options to the parameter execClass:

 * ModelicaExporterTestLF to convert only one system.
 
 * N44ConverterTest to convert the N44 CIM files from a directory with the same structure as the directory N44_CIM14_snapshots (one folder per day with all the CIM files)
 