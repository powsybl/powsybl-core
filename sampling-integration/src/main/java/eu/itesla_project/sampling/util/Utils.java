/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling.util;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
	public static MLDouble readMDoubleFromCSVFile(Path inFilePath, String mName, int nrows, int ncols, char delimiter) throws NumberFormatException, IOException {
		MLDouble mlDouble = new MLDouble( mName, new int[] {nrows, ncols} );
		CsvReader cvsReader = new CsvReader(inFilePath.toString());
		cvsReader.setDelimiter(delimiter);
		int i=0;
		while (cvsReader.readRecord()) {
			String[] rows = cvsReader.getValues();
			int j=0;
			for (String col : rows) {
				mlDouble.set(new Double(col), i, j);
				j++;
			}
			i++;
		}
		return mlDouble;
	}

	public static MLDouble matToMLDouble(double[][] dmat, String mName) throws NumberFormatException{
		if (dmat==null) {
			throw new NumberFormatException(".. must not be null");
		}
		MLDouble mlDouble = new MLDouble( mName, new int[] {dmat.length, dmat[0].length} );
		for (int i = 0; i < dmat.length; i++) {
			for (int j = 0; j < dmat[0].length; j++) {
				mlDouble.set(new Double(dmat[i][j]), i, j);
			}
		}
		return mlDouble;
	}

	public static MLDouble vectToMLDouble(double[] dvect, String mName) throws NumberFormatException{
		if (dvect==null) {
			throw new NumberFormatException(".. must not be null");
		}
		MLDouble mlDouble = new MLDouble( mName, new int[] {dvect.length, 1} );
		for (int i = 0; i < dvect.length; i++) {
			mlDouble.set(new Double(dvect[i]), i, 0);
		}
		return mlDouble;
	}

	public static MLDouble vectToMLDoubleRow(double[] dvect, String mName) throws NumberFormatException{
		if (dvect==null) {
			throw new NumberFormatException(".. must not be null");
		}
		MLDouble mlDouble = new MLDouble( mName, new int[] {1,dvect.length} );
		for (int i = 0; i < dvect.length; i++) {
			mlDouble.set(new Double(dvect[i]), 0, i);
		}
		return mlDouble;
	}

	public static double[][] readDoubleMatrixFromCSVFile(Path inFilePath,  int nrows, int ncols, char delimiter) throws NumberFormatException, IOException {
		double[][] mlDouble = new double[nrows][ncols];
		CsvReader cvsReader = new CsvReader(inFilePath.toString());
		cvsReader.setDelimiter(delimiter);
		int i=0;
		while (cvsReader.readRecord()) {
			String[] rows = cvsReader.getValues();
			int j=0;
			for (String col : rows) {
				mlDouble[i][j]=Double.parseDouble(col);
				j++;
				if (j>=ncols)
					break;
			}
			i++;
			if (i>=nrows)
				break;

		}
		return mlDouble;
	}

	public static double[] readDoubleArrayFromCSVFile(Path inFilePath,  int nrows, char delimiter) throws NumberFormatException, IOException {
		double[] mlDouble = new double[nrows];
		CsvReader cvsReader = new CsvReader(inFilePath.toString());
		cvsReader.setDelimiter(delimiter);
		int i=0;
		while (cvsReader.readRecord()) {
			String[] rows = cvsReader.getValues();
			for (String col : rows) {
				mlDouble[i]=Double.parseDouble(col);
			}
			i++;
			if (i>=nrows)
				break;
		}
		return mlDouble;
	}

	public static int countLines(Path path) throws IOException {
	    LineNumberReader reader  = new LineNumberReader(new FileReader(path.toFile()));
		int cnt = 0;
		String lineRead = "";
		while ((lineRead = reader.readLine()) != null) {}

		cnt = reader.getLineNumber();
		reader.close();
		return cnt;
	}

	//first element: names
	//second element:  double matrix
	public static List readHistoDataFromCsv(Path inFilePath, char delimiter) throws NumberFormatException, IOException {
		ArrayList retList=new ArrayList();

		int numberOfCSVLines=countLines(inFilePath);
		numberOfCSVLines=numberOfCSVLines-1; //take into account header

		CsvReader cvsReader = new CsvReader(inFilePath.toString());
		cvsReader.setDelimiter(delimiter);
		int i=0;
		cvsReader.readHeaders();


		String[] headers=cvsReader.getHeaders();
		retList.add(headers);

		double[][] mlDouble = new double[numberOfCSVLines][headers.length];

		while (cvsReader.readRecord()) {
			String[] rows = cvsReader.getValues();
			int j=0;
			for (String col : rows) {
				if (j>1) {

					mlDouble[i][j-2]=(col!=null)?Double.parseDouble(col):0.0;
				}
				j++;
			}
			i++;
			if (i>=numberOfCSVLines)
				break;

		}
		retList.add(mlDouble);
		return retList;
	}



    public static String MLCharToString(MLChar mlchar)
    {
        StringBuffer sb = new StringBuffer();
        for ( int m = 0; m < mlchar.getM(); m++ )
        {
           for ( int n = 0; n < mlchar.getN(); n++ )
           {
               sb.append( mlchar.getChar(m,n) );
           }
        }
        return sb.toString();
    }

    public static void writeWP41ContinuousInputMatFile(Path outputfile, double[][] idata, int nsamples, double ir, double k) throws IOException {

    	MLDouble dNsample = new MLDouble( "N_Sample", new double[] { nsamples },1 );
		MLDouble dIR = new MLDouble( "IR", new double[] { ir },1 );
		MLDouble dK = new MLDouble( "K", new double[] { k },1 );
		MLDouble data = Utils.matToMLDouble(idata, "X");

		List<MLArray> mlarray= new ArrayList<>();
		mlarray.add((MLArray) dNsample );
		mlarray.add((MLArray) dIR );
		mlarray.add((MLArray) dK );
		mlarray.add((MLArray) data );
		MatFileWriter writer = new MatFileWriter();
        writer.write(outputfile.toFile(), mlarray);
    }

//
//    public static void writeWP41ContinuousInputMatFile(Path outputfile, Wp41ContinuousParameters params) throws IOException {
//
//    	MLDouble dNsample = new MLDouble( "N_Sample", new double[] { params.getnSamples() },1 );
//		MLDouble dIR = new MLDouble( "IR", new double[] { params.getIr() },1 );
//		MLDouble dK = new MLDouble( "K", new double[] { params.getK() },1 );
//		MLDouble data = Utils.matToMLDouble(params.getHistoricalData(), "X");
//
//		List<MLArray> mlarray= new ArrayList<>();
//		mlarray.add((MLArray) dNsample );
//		mlarray.add((MLArray) dIR );
//		mlarray.add((MLArray) dK );
//		mlarray.add((MLArray) data );
//		MatFileWriter writer = new MatFileWriter();
//        writer.write(outputfile.toFile(), mlarray);
//    }
//



//    public static void writeWP41BinaryInputMatFile(Path outputfile, Wp41BinaryParameters params) throws IOException {
//
//    	MLDouble dNsample = new MLDouble( "N_SAMPLE", new double[] { params.getnSamples() },1 );
//		MLDouble data = Utils.matToMLDouble(params.getHistoricalData(), "X");
//
//		List<MLArray> mlarray= new ArrayList<>();
//		mlarray.add((MLArray) dNsample );
//		mlarray.add((MLArray) data );
//		MatFileWriter writer = new MatFileWriter();
//        writer.write(outputfile.toFile(), mlarray);
//    }
//
    public static void writeWP41BinaryIndependentSamplingInputFile(Path outputfile, double[] marginalExpectations) throws IOException {
		MLDouble data = Utils.vectToMLDoubleRow(marginalExpectations, "p");
		List<MLArray> mlarray= new ArrayList<>();
		mlarray.add((MLArray) data );
		MatFileWriter writer = new MatFileWriter();
        writer.write(outputfile.toFile(), mlarray);
    }



//    public static void writeWP43InputMatFile(Path outputfile, Wp43Parameters params) throws IOException {
//    	MLDouble dNt = new MLDouble( "nt", new double[] { params.getNt() },1 );
//		MLDouble dNb = new MLDouble( "nb", new double[] { params.getNb() },1 );
//		MLDouble dNl = new MLDouble( "nl", new double[] { params.getNl() },1 );
//		MLDouble dNm = new MLDouble( "nm", new double[] { params.getNm() },1 );
//		MLDouble bvm = Utils.matToMLDouble(params.getBvm(), "BVm");
//		//MLDouble bva = Utils.matToMLDouble(params.getBvm(), "BVa");
//		MLDouble t = Utils.vectToMLDouble(params.getT(), "t");
////		MLDouble lp1 = Utils.matToMLDouble(params.getLp1(), "LP1");
////		MLDouble lq1 = Utils.matToMLDouble(params.getLq1(), "LQ1");
//		MLDouble s1 = Utils.matToMLDouble(params.getS1(), "S1");
////		MLDouble h = Utils.vectToMLDouble(params.getH(), "H");
//		MLDouble delta = Utils.matToMLDouble(params.getDelta(), "delta");
//		MLDouble m = Utils.vectToMLDouble(params.getM(), "M");
//
//		List<MLArray> mlarray= new ArrayList<>();
//		mlarray.add((MLArray) dNt );
//		mlarray.add((MLArray) dNb );
//		mlarray.add((MLArray) dNl );
//		mlarray.add((MLArray) dNm );
//		mlarray.add((MLArray) bvm );
////		mlarray.add((MLArray) bva );
//		mlarray.add((MLArray) t );
////		mlarray.add((MLArray) lp1 );
////		mlarray.add((MLArray) lq1 );
//		mlarray.add((MLArray) s1 );
////		mlarray.add((MLArray) h );
//		mlarray.add((MLArray) m );
//		mlarray.add((MLArray) delta );
//
//		MatFileWriter writer = new MatFileWriter();
//        writer.write(outputfile.toFile(), mlarray);
//    }

    public static void dumpStringArray(Path outputFilePath, String[] stringArray) throws IOException {
        try (BufferedWriter os = Files.newBufferedWriter(outputFilePath, Charset.forName("UTF-8"))) {
        	for (int i = 0; i < stringArray.length; i++) {
        		os.write(stringArray[i]);
                os.newLine();
			}

        }
    }

    public static void copyFilesAndApplyPermissions(Path sourceDir, Path targetDir, List<String> filenames) throws IOException {
    	for (String fileName : filenames) {
			Files.copy(sourceDir.resolve(fileName),
					targetDir.resolve(fileName));

			try {
			 Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		        perms.add(PosixFilePermission.OWNER_READ);
		        perms.add(PosixFilePermission.OWNER_WRITE);
		        perms.add(PosixFilePermission.OWNER_EXECUTE);
		        perms.add(PosixFilePermission.GROUP_READ);
		        perms.add(PosixFilePermission.GROUP_WRITE);
		        perms.add(PosixFilePermission.GROUP_EXECUTE);
		        perms.add(PosixFilePermission.OTHERS_READ);
		        perms.add(PosixFilePermission.OTHERS_WRITE);
		        perms.add(PosixFilePermission.OTHERS_EXECUTE);
		        Files.setPosixFilePermissions(targetDir.resolve(fileName), perms);
			} catch (Throwable t) {

			}

		}

    }

	public static double[][] histoDataAsDoubleMatrix(ArrayTable<Integer,String,Float> hdTable) {

		int rowsSize=hdTable.rowKeySet().size();
        int colsSize=hdTable.columnKeySet().size();
        double[][] matFinal=new double[rowsSize][colsSize];
        for (int i = 0; i < rowsSize; i++) {
			for (int j = 0; j < colsSize; j++) {
				Float v=hdTable.get(i, j);
				matFinal[i][j]= ((v!=null) && (v.isNaN()==false)) ? v : 0.0f;
			}
		}
        return matFinal;
	}

	public static double[][] histoDataAsDoubleMatrixNew(ArrayTable<Integer,String,Float> hdTable) {
		int rowsSize=hdTable.rowKeySet().size();
        int colsSize=hdTable.columnKeySet().size();
        double[][] matFinal=new double[rowsSize][colsSize];
        int i=0;
        for (Integer rowKey: hdTable.rowKeyList()) {
        	int j=0;
			for (String colkey:hdTable.columnKeyList()) {
				Float v=hdTable.get(rowKey, colkey);
				matFinal[i][j]= ((v!=null) && (v.isNaN()==false)) ? v : 0.0f;
				j=j+1;
			}
			i=i+1;
		}
        return matFinal;
	}

	public static float[][] doubleToFloatMatrix(double[][] matrix) {
		float retMatrix[][]=new float[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				retMatrix[i][j]=(float) matrix[i][j];
			}
		}
		return retMatrix;
	}

	public static void dumpDoubleMatrix(double[][] matrix, Logger log) {
		log.info("Matrix size=" +matrix.length+" x " + matrix[0].length );
		for (double[] fs : matrix) {
			StringBuffer sb=new StringBuffer();
			for (double f : fs) {
				sb.append(f+", ");
			}
			log.info(sb.toString());
		}
	}

	public static void dumpWp41HistoData(Wp41HistoData hdata, Logger log) {
		log.info("Matrix size=" +hdata.getHdTable().rowKeySet().size()+" x " + hdata.getHdTable().columnKeySet().size() );
		Table<Integer,String,Float> table=hdata.getHdTable();
		StringBuffer sb=new StringBuffer();
		for (String colName : table.columnKeySet()) {
			sb.append(colName+", ");
		}
		log.info(sb.toString());

		for (Integer rowName: table.rowKeySet()) {
			sb=new StringBuffer();
			for (String colName : table.columnKeySet()) {
				sb.append(table.get(rowName, colName)+", ");
			}
			log.info(sb.toString());
		}

	}

    public static void dumpWp41HistoDataColumns(Wp41HistoData hdata, Path dir) throws IOException {
        List<String> columns = hdata.getHdTable().columnKeyList();
        try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve("columns.csv"), StandardCharsets.UTF_8)) {
            for (int i = 0; i < columns.size(); i++) {
                writer.write(Integer.toString(i));
                writer.write(";");
                writer.write(columns.get(i));
                writer.newLine();
            }
        }
    }

	public static Path createTmpDir(Path tmpDir, String prefix) throws IOException {
        if (tmpDir == null) {
            return Files.createTempDirectory(prefix);
        } else {
            return Files.createTempDirectory(tmpDir, prefix);
        }
    }
	public static void removeDir(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }


/*
    public static void writeWp41ContModule1Params(Path outputfile, Module1Params params) throws IOException {

		MLDouble dIR = new MLDouble( "IR", new double[] { params.getIr() },1 );
		MLDouble dK = new MLDouble( "K", new double[] { params.getK() },1 );
		MLDouble data = Utils.matToMLDouble(params.getHistoricalData(), "X");

		List<MLArray> mlarray= new ArrayList<>();
		mlarray.add((MLArray) dIR );
		mlarray.add((MLArray) dK );
		mlarray.add((MLArray) data );
		MatFileWriter writer = new MatFileWriter();
        writer.write(outputfile.toFile(), mlarray);
    }
*/
    public static void writeWp41ContModule1Mat(Path outputfile, double matrix[][]) throws IOException {
    	LOGGER.debug("writeWp41ContModule1Mat - writing wp41 input X matrix data to file {}", outputfile );
		MLDouble data = Utils.matToMLDouble(matrix, "X");
		List<MLArray> mlarray= new ArrayList<>();
		mlarray.add((MLArray) data );
		MatFileWriter writer = new MatFileWriter();
        writer.write(outputfile.toFile(), mlarray);
    	LOGGER.debug("writeWp41ContModule1Mat - X matrix data, written." );
    }

    public static double[][] joinDoubleMatrix(Path path1, Path path2, int rows, int cols1, int cols2) throws NumberFormatException, IOException {
    	int histoSize = rows;
		int loadsSize = cols1;
		int windGensSize = cols2;
		double[][] dataLoads = Utils.readDoubleMatrixFromCSVFile(
				path1, (int) histoSize,
				(int) loadsSize, ',');

		double[][] dataWind = Utils.readDoubleMatrixFromCSVFile(
				path2, (int) histoSize,
				(int) windGensSize, ',');

		//join tables
		double[][] data=new double[(int)histoSize][(int)loadsSize+(int)windGensSize];
		for (int i = 0; i < dataLoads.length; i++) {
			for (int j = 0; j < dataLoads[i].length; j++) {
				data[i][j]=dataLoads[i][j];
			}
		}
		for (int i = 0; i < dataWind.length; i++) {
			for (int j = 0; j < dataWind[i].length; j++) {
				data[i][j+loadsSize]=dataWind[i][j];
			}
		}
		return data;

    }

	public static float[] toFloatArray(double[] farray) {
		if (farray == null)
			return null;
		int n = farray.length;
		float[] darray = new float[n];
		for (int i = 0; i < n; i++) {
			darray[i] = (float) farray[i];
		}
		return darray;
	}

}
