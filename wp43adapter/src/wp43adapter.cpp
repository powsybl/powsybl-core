// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file wp43adapter.cpp
 * @author Quinary <itesla@quinary.com>
 * @version 1.1
 */

#include <iostream>   // std::cout
#include <sstream>
#include <stdexcept>
#include <pwd.h>
#include <log4cpp/Category.hh>
#include <log4cpp/PropertyConfigurator.hh>
#include <log4cpp/BasicConfigurator.hh>
#include <log4cpp/PatternLayout.hh>
#include <matio.h>
#include "ConfigFile.h"
#include <getopt.h>
#include <fstream>

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <iomanip>
#include <valarray>
#include <cmath>
#include <vector>
#include <boost/tokenizer.hpp>


using namespace std;
static int verbose_flag;
#pragma GCC diagnostic ignored "-Wattributes"
#include "api_eurostag.h"
#pragma GCC diagnostic warning "-Wattributes"
//if it was not present, it would not link against EUROSTAG libs !?!?! .....
extern "C" {
	int gBatchMode = 1;
	void ecrea1_() {
	}
	void ecmnou_() {
	}
}

static enum mat_ft mat_file_ver = MAT_FT_DEFAULT;
static enum matio_compression compression = MAT_COMPRESSION_ZLIB;// MAT_COMPRESSION_NONE;


std::string CONFIGFILENAME = "wp43adapter.properties";
std::string LOGCONFIGFILENAME = "wp43adapterlog.properties";

std::string DICT_LINES_CSV = "dict_lines.csv";
std::string DICT_BUSES_CSV = "dict_buses.csv";
std::string DICT_GENS_CSV  = "dict_gens.csv";

ConfigFile configFile=ConfigFile("");
log4cpp::Category *LOGGER;


/*
 * UTILS
 *
 */

/*
class BadConversion: public std::runtime_error {
public:
	BadConversion(std::string const& s) :
			std::runtime_error(s) {
	}
};

inline std::string stringify(float x) {
	std::ostringstream o;
	if (!(o << x))
		throw BadConversion("stringify(double)");
	return o.str();
}

inline std::string stringify(double x) {
	std::ostringstream o;
	if (!(o << x))
		throw BadConversion("stringify(double)");
	return o.str();
}
*/

/*
	NOTE:
	 in Eurostag API esg_getValues(int type, char *zid1, char *zid2, char *zidn, char *zid3, char *zid4, eustagUnit unit, float **values);
	 values is a pointer to a float array but BEWARE!!!  memory management done by Eurostag code,  no need to allocate anything ...
	 useless code !!: float *values = (float *)malloc(nbrTimes * sizeof(float));  //  *values returned by esg_getValues points to an internally managed memory address ...
	 drawback is that two consecutive calls to esg_getValues will destroy previous *values content !!!

 */

inline double string_to_double( const std::string& s )
 {
   std::istringstream i(s);
   double x;
   if (!(i >> x))
     return 0;
   return x;
 }

bool is_file_exist(const char *fileName)
{
    std::ifstream infile(fileName);
    return infile.good();
}

void splitLineMappingsEntry(const string& entry, string& iidmId, string& eurostagId) {
    //tokenize lines from mapping files
    //step1: split cim and eurostag sections
    vector< string > mapVec;
    boost::char_separator<char> sep(";");
    boost::tokenizer<boost::char_separator<char> > tokMap(entry, sep);
    mapVec.assign(tokMap.begin(),tokMap.end());
    iidmId=mapVec[0];
    eurostagId=mapVec[1];
}

void splitBusMappingsEntry(const string& entry, string& iidmId, string& eurostagId) {
    //tokenize lines from mapping files
    //step1: split cim and eurostag sections
    vector< string > mapVec;
    boost::char_separator<char> sep(";");
    boost::tokenizer<boost::char_separator<char> > tokMap(entry, sep);
    mapVec.assign(tokMap.begin(),tokMap.end());
    iidmId=mapVec[0];
    eurostagId=mapVec[1];
}

void splitGenMappingsEntry(const string& entry, string& iidmId, string& eurostagId,  double& inertia) {
    //tokenize lines from mapping files
    // CIM ID;EUROSTAG ID; INERTIA VALUE
    //step1: split cim and eurostag sections
    vector<string> mapVec;
    boost::char_separator<char> sep(";");
    boost::tokenizer<boost::char_separator<char> > tokMap(entry, sep);
    mapVec.assign(tokMap.begin(), tokMap.end());
    iidmId = mapVec[0];
    eurostagId = mapVec[1];
    inertia = string_to_double(mapVec[2]);
}


void splitEurostagId(const string branch, string& id1, string& id2, string& id3) {
    vector< string > partsVec;
    boost::char_separator<char> sep2("-");
    boost::tokenizer<boost::char_separator<char> > tokParts(branch, sep2);
    partsVec.assign(tokParts.begin(),tokParts.end());
    id1=partsVec[0];
    id2=partsVec[1];
    id3=partsVec[2];
}



int dataAsCSV( std::valarray<double> timeVector, std::vector< std::valarray<double> > &signal, const char *fileName) {
	FILE *aOutputFile = fopen(fileName, "w");
	for (int rows=0; rows < signal[0].size(); rows++) {
		fprintf(aOutputFile, "%.8g, ", timeVector[rows]);
		for (int cols=0; cols < signal.size(); cols++) {
			fprintf(aOutputFile, "%.8g", (signal[cols])[rows]);
			if (cols < (signal.size()-1)) {
				fprintf(aOutputFile, ", ");
			}
		}
		fprintf(aOutputFile, "\n");
	}
	fclose(aOutputFile);
	return 0;
}

/*
 * EUROSTAG INTEGRATION
 */


//infers from the Eurostag slow variables file 'names' , to be used when filter is enabled
int processEurostagSlowVariablesFile(std::string slowingVarFilePathS, std::set<std::string>* slowVarsNameSet) {
    log4cpp::Category *LOGGER=&log4cpp::Category::getInstance("slowvar");
    std::ifstream in(slowingVarFilePathS.c_str());
    if (!in.is_open()) {
        LOGGER->infoStream() << "No " << slowingVarFilePathS << " file found: no line/bus/generator will be skipped.";
        return 1;
    }

    LOGGER->info("Processing slowing variables file: " + slowingVarFilePathS);

    string line;
    int noline=0;
    while (std::getline(in,line))
    {
        //e.g
        //0.5000000E+02;FA,------,--------,--------------;FR,  5199,G0000159, 0.1590281E-03;FA,------,--------,--------------;FR, 12520,N0000982, 0.0000000E+00
        LOGGER->debug("  slowingvar file line:  " + line);
        vector< string > mapVec;
        boost::char_separator<char> sep(";");
        boost::tokenizer<boost::char_separator<char> > tokMap(line, sep);
        mapVec.assign(tokMap.begin(),tokMap.end());
        for (int i = 1; i < mapVec.size(); i=i+1) {
            //LOGGER->info("  " + mapVec[i]);
            vector< string > mapVecInner;
            boost::char_separator<char> sepInner(",");
            boost::tokenizer<boost::char_separator<char> > tokMapInner(mapVec[i], sepInner);
            mapVecInner.assign(tokMapInner.begin(),tokMapInner.end());
            if (mapVecInner.size()>2) {
                LOGGER->debug("  slow var name: " + mapVecInner[2]);
                slowVarsNameSet->insert(mapVecInner[2]);
            }
        }
    }// end loop
    LOGGER->infoStream() << "found " << slowVarsNameSet->size() << " 'names' in " << slowingVarFilePathS;
    std::set<std::string>::iterator it;
    for (it = slowVarsNameSet->begin(); it != slowVarsNameSet->end(); ++it)
    {
        string s1 = *it;
        LOGGER->info(s1);
    }
    in.close();
    return 0;
}


class SlowvarData {
    bool slowDataFound;
    set<string> slowVarsNameSet;

public:
    SlowvarData(string slowingVarFilePathS);

    set<string> const &getSlowVarsNameSet() const {
        return slowVarsNameSet;
    }

    bool isSlowDataFound() const {
        return slowDataFound;
    }

    bool includesLine(const string &line);

    bool includesLine(const string &id1, const string &id2, const string &id3);

    bool includesBus(const string &bus);

    bool includesMachine(const string &machine);
};

bool SlowvarData::includesLine(const string& line) {
    vector< string > partsVec;
    boost::char_separator<char> sep2("-");
    boost::tokenizer<boost::char_separator<char> > tokParts(line, sep2);
    partsVec.assign(tokParts.begin(),tokParts.end());

    return ((slowVarsNameSet.find(partsVec[0]) != slowVarsNameSet.end())
            || (slowVarsNameSet.find(partsVec[1]) != slowVarsNameSet.end()));
}

bool SlowvarData::includesLine(const string& id1, const string& id2, const string &id3) {
    return ((slowVarsNameSet.find(id1) != slowVarsNameSet.end())
            || (slowVarsNameSet.find(id2) != slowVarsNameSet.end()));
}


bool SlowvarData::includesBus(const string& bus) {
    return (slowVarsNameSet.find(bus) != slowVarsNameSet.end());
}

bool SlowvarData::includesMachine(const string& machine) {
    return (slowVarsNameSet.find(machine) != slowVarsNameSet.end());
}


SlowvarData::SlowvarData(string slowingVarFilePathS) {
    int slowingVariablesFound=processEurostagSlowVariablesFile(slowingVarFilePathS, &slowVarsNameSet);
    slowDataFound = false;
    if (slowingVariablesFound == 0)
        slowDataFound = true;
}


int processEurostagResultsOverload(char const *caseFolder, char const *casePrefix, valarray<double> **timesVect, vector<valarray<double> > **signalMatrix, bool filterEnabled, SlowvarData& slowvarData) {
    *signalMatrix= new vector< valarray<double>  > ();

    if (esg_init() != 0)
        throw std::runtime_error("esg_init");

	std::string caseFilePathS=std::string(caseFolder)+ "/" + std::string(casePrefix);
    LOGGER->info("Casepath: " + caseFilePathS);
    if (esg_loadCase((char*)caseFilePathS.c_str()) != 0)
        throw std::runtime_error("esg_loadCase");

	int nbrTimes;
    if (esg_getTimesNumber(&nbrTimes) != 0)
        throw std::runtime_error("esg_getTimesNumber");
	LOGGER->infoStream() <<  "times size: " << nbrTimes;

	float *values;
	//1st step: get time values from Eurostag results
	//note: we are assuming that the cardinality of time vector and signals vectors are the same (otherways ?!?!? ... )
    int typeNum = 0;
	char* zid11 = (char*) "        ";
	char* zid12 = (char*) "        ";
	char* zid1n = (char*) "        ";
	char* zid13 = (char*) "        ";
	char* zid14 = (char*) "        ";
    if (esg_getValues(typeNum, zid11, zid12, zid1n, zid13, zid14, ESG_PU, &values) != 0)
        throw std::runtime_error("esg_getValues");

    *timesVect=new std::valarray<double>(nbrTimes);
	for (int i = 0; i < nbrTimes; ++i) {
		(**timesVect)[i]=values[i];
	}

	// 2nd step: retrieve signal values from Eurostag results
	// this is actually driven by dict_lines.csv
	// dict_lines.csv file must contain all and only mappings eurostag_id,cim_id for lines
	// e.g. _BACALP61-BRUGEP62-1_AC;N0000002-N0000016-1
	// for each line, retrieve first P, than Q, than compute apparent Power

	std::string dictLinesFilePathS = std::string(caseFolder) + "/" + DICT_LINES_CSV;
    LOGGER->info("Processing mapping file: " + dictLinesFilePathS);
	std::ifstream in(dictLinesFilePathS.c_str());
	if (!in.is_open()) return 1;
	string line;
    int noline=0;
    int noSkippedLines=0;
    while (std::getline(in,line))
    {
        string iidmId, eurostagId;
        string eId1,eId2,eId3;

        splitLineMappingsEntry(line,iidmId,eurostagId);
        splitEurostagId(eurostagId, eId1, eId2, eId3);

        bool skipLine=true;
        if ((filterEnabled == false) || (slowvarData.includesLine(eId1,eId2,eId3))) {
            skipLine = false;
            LOGGER->infoStream() << "line: " << iidmId << " -> " << eurostagId << " NOT SKIPPED";

            char *zid1 = (char *) eId1.c_str();
            char *zid2 = (char *) eId2.c_str();
            char zidn[9];
            sprintf(zidn, "%-8s", eId3.c_str());
            char *zid3 = (char *) "        ";
            char *zid4 = (char *) "        ";

            //get P curve from Eurostag results
            int typeP = 4;
            if (esg_getValues(typeNum, zid1, zid2, zidn, zid3, zid4, ESG_SI, &values) != 0)
                throw std::runtime_error("esg_getValues");

            std::valarray<double> aP(nbrTimes);
            for (int i = 0; i < nbrTimes; ++i) {
                aP[i] = values[i];
            }

            //get Q curve from Eurostag results
            int typeQ = 5;
            if (esg_getValues(typeQ, zid1, zid2, zidn, zid3, zid4, ESG_SI, &values) != 0)
                throw std::runtime_error("esg_getValues");

            std::valarray<double> aQ(nbrTimes);
            for (int i = 0; i < nbrTimes; ++i) {
                aQ[i] = values[i];
            }


            //compute the apparent power S=sqrt(P^2 + Q^2)
            std::valarray<double> apparentPower = std::sqrt(std::pow(aP, 2.0) + std::pow(aQ, 2.0));
            (**signalMatrix).push_back(apparentPower);
        } else {
            noSkippedLines=noSkippedLines+1;
            LOGGER->infoStream() << "line: " << iidmId << " -> " << eurostagId << " SKIPPED";
        }

        noline=noline+1;
		//destroys explicitly .. aP and aQ, at each iteration
//		~aP;
//		~aQ;
    }// end loop
    LOGGER->infoStream() << "processed:  " <<  noline << " lines";
    LOGGER->infoStream() << "skipped:  " <<  noSkippedLines << " lines";
    if ((noline-noSkippedLines) == 0) {
        LOGGER->warnStream() << " all lines skipped; no input data for the overload index";
    }

    if (esg_unloadCase() != 0)
        throw std::runtime_error("esg_unloadCase error");
	return 0;
}


int processEurostagResultsUnderOverVoltage(const char* caseFolder, const char* casePrefix, std::valarray<double> **timesVect, vector< valarray<double>  >  **signalMatrix, bool filterEnabled, SlowvarData& slowvarData) {
    *signalMatrix= new vector< valarray<double>  > ();

    if (esg_init() != 0)
        throw std::runtime_error("esg_init");

    std::string caseFilePathS=std::string(caseFolder)+ "/" + std::string(casePrefix);
    LOGGER->info("Casepath: " + caseFilePathS);
    if (esg_loadCase((char*)caseFilePathS.c_str()) != 0)
        throw std::runtime_error("esg_loadCase");

    int nbrTimes;
    if (esg_getTimesNumber(&nbrTimes) != 0)
        throw std::runtime_error("esg_getTimesNumber");
    LOGGER->infoStream() <<  "times size: " << nbrTimes;

    float *values;
    //1st step: get time values from Eurostag results
    //note: we are assuming that the cardinality of time vector and signals vectors are the same (otherways ?!?!? ... )
    int typeNum = 0;
    char* zid11 = (char*) "        ";
    char* zid12 = (char*) "        ";
    char* zid1n = (char*) "        ";
    char* zid13 = (char*) "        ";
    char* zid14 = (char*) "        ";
    if (esg_getValues(typeNum, zid11, zid12, zid1n, zid13, zid14, ESG_PU, &values) != 0)
        throw std::runtime_error("esg_getValues");

    *timesVect=new std::valarray<double>(nbrTimes);
    for (int i = 0; i < nbrTimes; ++i) {
        (**timesVect)[i]=values[i];
    }

    // 2nd step: retrieve signal values from Eurostag results
    // this is actually driven by dict_lines.csv
    // dict_lines.csv file must contain all and only mappings eurostag_id,cim_id for lines
    // e.g. _BACALP61-BRUGEP62-1_AC;N0000002-N0000016-1
    // for each line, retrieve first P, than Q, than compute apparent Power

    std::string dictLinesFilePathS = std::string(caseFolder) + "/" + DICT_BUSES_CSV;
    LOGGER->info("Processing mapping file: " + dictLinesFilePathS);
    std::ifstream in(dictLinesFilePathS.c_str());
    if (!in.is_open()) return 1;
    string line;
    int noline=0;
    int noSkippedLines=0;
    while (std::getline(in,line))
    {
        string iidmId, eurostagId;
        splitBusMappingsEntry(line,iidmId,eurostagId);

        bool skipLine=true;
        if ((filterEnabled == false) || (slowvarData.includesBus(eurostagId))) {
            skipLine = false;
            LOGGER->infoStream() << "bus: " << iidmId << " -> " << eurostagId << " NOT SKIPPED";

            char *zid1 = (char *) eurostagId.c_str();
            char *zid2 = (char *) "        ";
            char *zid3 = (char *) "        ";
            char *zid4 = (char *) "        ";
            char *zid5 = (char *) "        ";

            //get Voltage curve from Eurostag results
            int typeP = 10;
            if (esg_getValues(typeP, zid1, zid2, zid3, zid4, zid5, ESG_SI, &values) != 0)
                throw std::runtime_error("esg_getValues");

            double sumVoltages = 0.0;
            std::valarray<double> Voltage(nbrTimes);
            for (int i = 0; i < nbrTimes; ++i) {
                Voltage[i] = values[i];
                sumVoltages = sumVoltages + Voltage[i];
            }
            //if (sumVoltages > 0.0) {
                (**signalMatrix).push_back(Voltage);
            //}
        } else {
            noSkippedLines=noSkippedLines+1;
            LOGGER->infoStream() << "bus: " << iidmId << " -> " << eurostagId << " SKIPPED";
        }

        noline=noline+1;
        //destroys explicitly .. aP and aQ, at each iteration
//		~aP;
//		~aQ;
    }// end loop
    LOGGER->infoStream() << "processed:  " <<  noline << " buses";
    LOGGER->infoStream() << "skipped:  " <<  noSkippedLines << " buses";
    if ((noline-noSkippedLines) == 0) {
        LOGGER->warnStream() << " all buses skipped; no input data for the underovervoltage index";
    }


    if (esg_unloadCase() != 0)
        throw std::runtime_error("esg_unloadCase error");
    return 0;
}

int processEurostagResultsSmallSignal(const char* caseFolder, const char* casePrefix, std::valarray<double> **timesVect, vector< valarray<double>  >  **signalMatrix, bool filterEnabled, SlowvarData& slowvarData) {
    if (esg_init() != 0)
        throw std::runtime_error("esg_init");

	std::string caseFilePathS=std::string(caseFolder)+ "/" + std::string(casePrefix);
    LOGGER->info("Casepath: " + caseFilePathS);
    if (esg_loadCase((char*)caseFilePathS.c_str()) != 0)
        throw std::runtime_error("esg_loadCase");

	int nbrTimes;
    if (esg_getTimesNumber(&nbrTimes) != 0)
        throw std::runtime_error("esg_getTimesNumber");
	LOGGER->infoStream() <<  "times size: " << nbrTimes;

	float *values;
	//1st step: get time values from Eurostag results
	//note: we are assuming that the cardinality of time vector and signals vectors are the same (otherways ?!?!? ... )
	char* zid11 = (char*) "        ";
	char* zid12 = (char*) "        ";
	char* zid1n = (char*) "        ";
	char* zid13 = (char*) "        ";
	char* zid14 = (char*) "        ";
	int typeP = 0;
	if (esg_getValues(typeP, zid11, zid12, zid1n, zid13, zid14, ESG_PU, &values) != 0)
        throw std::runtime_error("esg_getValues error");

    *timesVect=new std::valarray<double>(nbrTimes);
    for (int i = 0; i < nbrTimes; ++i) {
		(**timesVect)[i]=values[i];
	}

	// 2nd step: retrieve signal values from Eurostag results
	// this is actually driven by dict_lines.csv
	// dict_lines.csv file must contain all and only mappings eurostag_id,cim_id for lines
	// e.g. _BACALP61-BRUGEP62-1_AC;N0000002-N0000016-1
	// for each line, retrieve  P
	*signalMatrix= new vector< valarray<double>  > ();

	std::string dictLinesFilePathS = std::string(caseFolder) + "/" + DICT_LINES_CSV;
	std::ifstream in(dictLinesFilePathS.data());
	if (!in.is_open()) return 1;
	string line;
    int noline=0;
    int noSkippedLines=0;
    LOGGER->info("Processing mapping file: " + dictLinesFilePathS);
    while (std::getline(in,line))
    {
        string iidmId, eurostagId;
        string eId1,eId2,eId3;

        splitLineMappingsEntry(line,iidmId,eurostagId);
        splitEurostagId(eurostagId, eId1, eId2, eId3);

        char* zid1 = (char*) eId1.c_str();
		char* zid2 = (char*) eId2.c_str();
		char zidn[9];
		sprintf(zidn,"%-8s", eId3.c_str());
		char* zid3 = (char*) "        ";
		char* zid4 = (char*) "        ";

		noline=noline+1;

		bool skipLine=true;
		if ((filterEnabled==false) || (slowvarData.includesLine(eId1,eId2,eId3))) {
			skipLine=false;

			LOGGER->infoStream() << "line: " <<  iidmId << " -> "<< eurostagId << " NOT SKIPPED";
			//get P curve from Eurostag results
			int typeP = 4;
			if (esg_getValues(typeP, zid1, zid2, zidn, zid3, zid4, ESG_SI, &values) != 0)
                throw std::runtime_error("esg_getValues error");
			std::valarray<double> aP(nbrTimes);
			for (int i = 0; i < nbrTimes; ++i) {
				aP[i]=values[i];
			}

			(**signalMatrix).push_back(aP);

		} else {
			noSkippedLines=noSkippedLines+1;
            LOGGER->infoStream() << "line: " <<  iidmId << " -> "<< eurostagId << " SKIPPED";
		}
		//destroys explicitly .. aP and aQ, at each iteration
    }// end loop
    in.close();

    LOGGER->infoStream() << "processed " <<  noline << " lines; skipped "<< noSkippedLines << " lines.";
    if ((noline-noSkippedLines) == 0) {
        LOGGER->warnStream() << " all lines skipped; no input data for the smallsignal index";
    }


    if (esg_unloadCase() != 0)
        throw std::runtime_error("esg_unloadCase error");
	return 0;
}

int processEurostagResultsTransient(const char *caseFolder, const char *casePrefix, std::valarray<double> **timesVect, vector<valarray<double> > **signalMatrix, vector<double> **inertiaVect, bool filterEnabled, SlowvarData& slowvarData) {
    if (esg_init() != 0)
        throw std::runtime_error("esg_init");

    std::string caseFilePathS=std::string(caseFolder)+ "/" + std::string(casePrefix);
	LOGGER->info("Casepath: " + caseFilePathS);
    if (esg_loadCase((char*)caseFilePathS.c_str()) != 0)
        throw std::runtime_error("esg_loadCase");

    int nbrTimes;
    if (esg_getTimesNumber(&nbrTimes) != 0)
        throw std::runtime_error("esg_getTimesNumber");
	LOGGER->infoStream() <<  "times size: " << nbrTimes;

	//1st step: get time values from Eurostag results
	//note: we are assuming that the cardinality of time vector and signals vectors are the same (otherways ?!?!? ... )
    float *values;
    int typeP = 0;
    char* zid11 = (char*) "        ";
	char* zid12 = (char*) "        ";
	char* zid1n = (char*) "        ";
	char* zid13 = (char*) "        ";
	char* zid14 = (char*) "        ";
    if (esg_getValues(typeP, zid11, zid12, zid1n, zid13, zid14, ESG_PU, &values) != 0)
        throw std::runtime_error("esg_getValues");

    *timesVect=new std::valarray<double>(nbrTimes);
	for (int i = 0; i < nbrTimes; ++i) {
		(**timesVect)[i]=values[i];
	}

	// 2nd step: retrieve signal values from Eurostag results
	// this is actually driven by dict_gens.csv
	// dict_gens.csv file must contain all and only mappings cim_id,eurostag_id,machine inertia H
	// e.g. BLAYA7GR4_NGU_SM;G0000000;6.3
	// for each machine, retrieve  angle time serie
    *signalMatrix = new vector<valarray<double> >();
    *inertiaVect = new vector<double>();

	std::string dictLinesFilePathS = std::string(caseFolder) + "/" + DICT_GENS_CSV;
    LOGGER->info("Processing mapping file: " + dictLinesFilePathS);
	std::ifstream in(dictLinesFilePathS.c_str());
	if (!in.is_open())
        throw std::runtime_error("could not open file " + dictLinesFilePathS);
	string line;
    int noline=0;
    int noSkippedLines=0;
    while (std::getline(in,line))
    {
        string iidmId, eurostagId;
        double inertia;
        splitGenMappingsEntry(line, iidmId, eurostagId, inertia);

        bool skipLine=true;
        if ((filterEnabled == false) || (slowvarData.includesMachine(eurostagId))) {
            skipLine = false;

            LOGGER->infoStream() << "gen: " << iidmId << " (" << eurostagId << ");  inertia: " << inertia << " NOT SKIPPED";

            //get 'angular position' curve (type 36) from Eurostag results
            int typeNum = 36;
            char *zid1 = (char *) eurostagId.c_str();
            char *zid2 = (char *) "        ";
            char *zidn = (char *) "        ";
            char *zid3 = (char *) "        ";
            char *zid4 = (char *) "        ";
            if (esg_getValues(typeNum, zid1, zid2, zidn, zid3, zid4, ESG_SI, &values) != 0)
                throw std::runtime_error("esg_getValues error");

            //store this generator time series data
            std::valarray<double> eurostagCurve(nbrTimes);
            for (int i = 0; i < nbrTimes; ++i) {
                eurostagCurve[i] = values[i];
            }
            (**signalMatrix).push_back(eurostagCurve);
            (**inertiaVect).push_back(inertia);
        } else {
            noSkippedLines=noSkippedLines+1;
            LOGGER->infoStream() << "gen: " << iidmId << " (" << eurostagId << ");  inertia: " << inertia << " SKIPPED";

        }
        noline=noline+1;
    }// end loop

    LOGGER->infoStream() << "Processed " <<  noline << " generators.";
    LOGGER->infoStream() << "Skipped "<< noSkippedLines << " generators.";
    if ((noline-noSkippedLines) == 0) {
        LOGGER->warnStream() << " all generators skipped; no input data for the transient index";
    }


    if (esg_unloadCase() != 0)
        throw std::runtime_error("esg_unloadCase error");
	return 0;
}


/*
 * MAT files building
 */
int overloadPrepareInput(string moutputfile, string caseFolder, string casePrefix, SlowvarData slowvarData) {
	std::string indexerId = "overload";
	LOGGER->infoStream() << "Indexer name:  " << indexerId;

    bool enableFilter=false;
    string enableFilter_s = (const string) configFile.Value(indexerId,"filter","");
    if (enableFilter_s.compare("true") == 0) {
        enableFilter=true;
    }
    LOGGER->infoStream() << " filter enabled:  " << enableFilter;

    string p_s;
    string d_s;

    try {
        p_s = (const string) configFile.Value(indexerId, "p");
        d_s = (const string) configFile.Value(indexerId, "d");
    } catch (const char* errormsg) {
        LOGGER->error(errormsg);
        return 1;
    }
	double p=string_to_double(p_s);
	double d=string_to_double(d_s);
	LOGGER->infoStream() << " p: " << p;
	LOGGER->infoStream() << " d: " << d;

	std::valarray<double> *timesVect;
	vector< valarray<double>  >  *signalData;

    processEurostagResultsOverload(caseFolder.c_str(), casePrefix.c_str(), &timesVect, &signalData, enableFilter, slowvarData);
	if (verbose_flag) {
        string csvFilePath = moutputfile + "_"+ indexerId+".csv";
		dataAsCSV(*timesVect, *signalData, csvFilePath.c_str());
	}

	int timesVectSize=(*timesVect).size();
	if (timesVectSize == 0) {
		LOGGER->errorStream() << " no samples in Eurostag simulation results ";
		exit(-1);
	}
	LOGGER->infoStream() << " simulation index time window s_t=["  <<  (*timesVect)[0] << "," << (*timesVect)[timesVectSize-1] << "]";
	LOGGER->infoStream() << " simulation sampling size: " << timesVect->size();

	int err = 0, i;
	mat_t *mat;
	matvar_t *matvar;


    string matOutputfile=moutputfile + "_"+ indexerId+".mat";
    LOGGER->infoStream() << "writing mat file '"+  matOutputfile + "'";

    //mat = Mat_Open(fileName,MAT_ACC_RDWR);
	mat = Mat_CreateVer(matOutputfile.c_str(), NULL, mat_file_ver);

	if (mat) {

		size_t dims_single[1] = {  1 };
		matvar = Mat_VarCreate("p", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &p, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);

		matvar = Mat_VarCreate("d", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &d, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);


		//1) write t
		int tsize=(*timesVect).size();

		size_t dims_t[1] = { tsize};
		//double t[tsize];
		double *t=new double[tsize];
		for (int i = 0; i < tsize; i++) {
			t[i] = (*timesVect)[i];
		}

		//free mem from function call
		delete timesVect;

		matvar = Mat_VarCreate("t", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_t, t, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);
		delete t;


		//3) write S1
		int S1size=signalData->size();
		size_t dims_S1[2] = {tsize, S1size};
		double *S1=new double[tsize*S1size];
		int i=0;
		for (int lineIndex = 0; lineIndex < S1size; ++lineIndex) {
			for (int timeIndex = 0; timeIndex < tsize; ++timeIndex) {
				S1[i]=((*signalData)[lineIndex])[timeIndex];
				i=i+1;
			}
		}
		matvar = Mat_VarCreate("S", MAT_C_DOUBLE, MAT_T_DOUBLE, 2, dims_S1, S1, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);
		delete S1;


		Mat_Close(mat);

		//free mem from function call
		delete signalData;
		LOGGER->infoStream() << " mat file '"+  moutputfile +  "', written.";

	} else {
		LOGGER->errorStream() << "Could not write mat file "<< moutputfile;
		return 1;
	}
	return 0;
}


int underOverVoltagePrepareInput(string moutputfile, string caseFolder, string casePrefix, SlowvarData slowvarData) {
    std::string indexerId = "underovervoltage";
    LOGGER->infoStream() << "Indexer name:  " << indexerId;

    bool enableFilter=false;
    string enableFilter_s = (const string) configFile.Value(indexerId,"filter","");
    if (enableFilter_s.compare("true") == 0) {
        enableFilter=true;
    }
    LOGGER->infoStream() << " filter enabled:  " << enableFilter;



    string p_s;
    string d_s;

    try {
        p_s = (const string) configFile.Value(indexerId, "p");
        d_s = (const string) configFile.Value(indexerId, "d");
    } catch (const char* errormsg) {
        LOGGER->error(errormsg);
        return 1;
    }
    double p=string_to_double(p_s);
    double d=string_to_double(d_s);
    LOGGER->infoStream() << " p: " << p;
    LOGGER->infoStream() << " d: " << d;

    std::valarray<double> *timesVect;
    vector< valarray<double>  >  *signalData;

    processEurostagResultsUnderOverVoltage(caseFolder.c_str(), casePrefix.c_str(), &timesVect, &signalData,enableFilter,slowvarData);
    if (verbose_flag) {
        string csvFilePath = moutputfile + "_"+ indexerId+".csv";
        dataAsCSV(*timesVect, *signalData, csvFilePath.c_str());
    }

    int timesVectSize=(*timesVect).size();
    if (timesVectSize == 0) {
        LOGGER->errorStream() << " no samples in Eurostag simulation results ";
        exit(-1);
    }
    LOGGER->infoStream() << " simulation index time window s_t=["  <<  (*timesVect)[0] << "," << (*timesVect)[timesVectSize-1] << "]";
    LOGGER->infoStream() << " simulation sampling size: " << timesVect->size();

    int err = 0, i;
    mat_t *mat;
    matvar_t *matvar;

    string matOutputfile=moutputfile + "_"+ indexerId+".mat";
    LOGGER->infoStream() << "writing mat file '"+  matOutputfile + "'";

    //mat = Mat_Open(fileName,MAT_ACC_RDWR);
    mat = Mat_CreateVer(matOutputfile.c_str(), NULL, mat_file_ver);

    if (mat) {

        size_t dims_single[1] = {  1 };
        matvar = Mat_VarCreate("p", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &p, 0);
        Mat_VarWrite(mat, matvar, compression);
        Mat_VarFree(matvar);

        matvar = Mat_VarCreate("d", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &d, 0);
        Mat_VarWrite(mat, matvar, compression);
        Mat_VarFree(matvar);


        //1) write t
        int tsize=(*timesVect).size();

        size_t dims_t[1] = { tsize};
        //double t[tsize];
        double *t=new double[tsize];
        for (int i = 0; i < tsize; i++) {
            t[i] = (*timesVect)[i];
        }

        //free mem from function call
        delete timesVect;

        matvar = Mat_VarCreate("t", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_t, t, 0);
        Mat_VarWrite(mat, matvar, compression);
        Mat_VarFree(matvar);
        delete t;


        //3) write S1
        int S1size=signalData->size();
        size_t dims_S1[2] = {tsize, S1size};
        double *S1=new double[tsize*S1size];
        int i=0;
        for (int lineIndex = 0; lineIndex < S1size; ++lineIndex) {
            for (int timeIndex = 0; timeIndex < tsize; ++timeIndex) {
                S1[i]=((*signalData)[lineIndex])[timeIndex];
                i=i+1;
            }
        }
        matvar = Mat_VarCreate("V", MAT_C_DOUBLE, MAT_T_DOUBLE, 2, dims_S1, S1, 0);
        Mat_VarWrite(mat, matvar, compression);
        Mat_VarFree(matvar);
        delete S1;


        Mat_Close(mat);

        //free mem from function call
        delete signalData;
        LOGGER->infoStream() << " mat file '"+  moutputfile +  "', written.";

    } else {
        LOGGER->errorStream() << "Could not write mat file "<< moutputfile;
        return 1;
    }
    return 0;
}




int smallSignalPrepareInput(string moutputfile, string caseFolder, string casePrefix, SlowvarData slowvarData) {
	string indexerId = "smallsignal";
	LOGGER->infoStream() << "Indexer name:  " << indexerId;

    bool enableFilter=false;
    string enableFilter_s = (const string) configFile.Value(indexerId,"filter","");
    if (enableFilter_s.compare("true") == 0) {
        enableFilter=true;
    }
    LOGGER->infoStream() << " filter enabled:  " << enableFilter;

    string step_min_s=(const string) configFile.Value(indexerId, "step_min");
	double step_min=string_to_double(step_min_s);
	LOGGER->infoStream() << "small signal";
	LOGGER->infoStream() << " step_min="  <<  step_min;

	string var_min_s=(const string) configFile.Value(indexerId, "var_min");
	double var_min=string_to_double(var_min_s);
	LOGGER->infoStream() << " var_min="  <<  var_min;

	std::string f_1_s=(const std::string) configFile.Value(indexerId, "f_1");
	std::string f_2_s=(const std::string) configFile.Value(indexerId, "f_2");
	double f_1=string_to_double(f_1_s);
	double f_2=string_to_double(f_2_s);
	LOGGER->infoStream() << " f=["  <<  f_1 << "," << f_2 << "]";

	std::string d_1_s=(const std::string) configFile.Value(indexerId, "d_1");
	std::string d_2_s=(const std::string) configFile.Value(indexerId, "d_2");
	std::string d_3_s=(const std::string) configFile.Value(indexerId, "d_3");
	double d_1=string_to_double(d_1_s);
	double d_2=string_to_double(d_2_s);
	double d_3=string_to_double(d_3_s);
	LOGGER->infoStream() << " damp=["  <<  d_1 << "," << d_2 << "," << d_3 << "]";

	std::string nm_s=(const std::string) configFile.Value(indexerId, "nm");
	LOGGER->infoStream() << " Nm="  <<  nm_s;

        double f_instant = string_to_double((const std::string) configFile.Value(indexerId, "f_instant"));
	LOGGER->infoStream() << " f_instant="  << f_instant;

        double f_duration = string_to_double((const std::string) configFile.Value(indexerId, "f_duration"));
	LOGGER->infoStream() << " f_duration="  << f_duration;	

	std::valarray<double> *timesVect;
	vector< valarray<double>  >  *signalData;

	processEurostagResultsSmallSignal(caseFolder.c_str(), casePrefix.c_str(), &timesVect, &signalData, enableFilter,slowvarData);
	if (verbose_flag) {
        string csvFilePath = moutputfile + "_"+ indexerId+".csv";
        dataAsCSV(*timesVect, *signalData, csvFilePath.c_str());
	}
	int timesVectSize=(*timesVect).size();
	if (timesVectSize == 0) {
		LOGGER->errorStream() << " no samples in Eurostag simulation results ";
		exit(-1);
	}
	LOGGER->infoStream() << " simulation index time window s_t=["  <<  (*timesVect)[0] << "," << (*timesVect)[timesVectSize-1] << "]";
	LOGGER->infoStream() << " simulation sampling size: " << timesVect->size();


	int err = 0, i;
	mat_t *mat;
	matvar_t *matvar;


    string matOutputfile=moutputfile + "_"+ indexerId+".mat";
    LOGGER->infoStream() << "writing mat file '"+  matOutputfile + "'";
	//mat = Mat_Open(fileName,MAT_ACC_RDWR);
	mat = Mat_CreateVer(matOutputfile.c_str(), NULL, mat_file_ver);

	if (mat) {

		// step_min
		size_t dims_single[1] = {  1 };
		matvar = Mat_VarCreate("step_min", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &step_min, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);

		// var_min
		matvar = Mat_VarCreate("var_min", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &var_min, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);

		// t
		int tsize=(*timesVect).size();

		size_t dims_t[1] = { tsize};
		double *t=new double[tsize];
		for (int i = 0; i < tsize; i++) {
			t[i] = (*timesVect)[i];
		}
		delete timesVect;

		matvar = Mat_VarCreate("t", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_t, t, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);
		delete t;

	
		// d
		int dampsize=3;
		size_t dims_damp[2] = {1, dampsize};
		double damp[dampsize];
		damp[0]=d_1;
		damp[1]=d_2;
		damp[2]=d_3;
		matvar = Mat_VarCreate("d", MAT_C_DOUBLE, MAT_T_DOUBLE, 2, dims_damp, damp, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);

		// f
		int fsize=2;
		size_t dims_f[2] = {1, fsize};
		double f[fsize];
		f[0]=f_1;
		f[1]=f_2;
		matvar = Mat_VarCreate("f", MAT_C_DOUBLE, MAT_T_DOUBLE, 2, dims_f, f, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);

		// Nm
		if (nm_s.compare("") != 0) {
			double nmodesVal=string_to_double(nm_s);
			matvar = Mat_VarCreate("Nm", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &nmodesVal, 0);
			Mat_VarWrite(mat, matvar, compression);
			Mat_VarFree(matvar);
		}

                matvar = Mat_VarCreate("f_instant", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &f_instant, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);

                matvar = Mat_VarCreate("f_duration", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_single, &f_duration, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);

		// LP1
		int S1size=signalData->size();
		size_t dims_S1[2] = {tsize, S1size};
		double *S1=new double[tsize*S1size];
		int i=0;
		for (int lineIndex = 0; lineIndex < S1size; ++lineIndex) {
			for (int timeIndex = 0; timeIndex < tsize; ++timeIndex) {
				S1[i]=((*signalData)[lineIndex])[timeIndex];
				i=i+1;
			}
		}
		matvar = Mat_VarCreate("LP1", MAT_C_DOUBLE, MAT_T_DOUBLE, 2, dims_S1, S1, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);
		delete S1;


		Mat_Close(mat);

		//free mem from function call
		delete signalData;
		LOGGER->infoStream() << " mat file '"+  moutputfile +  "', written.";

	} else {
		LOGGER->errorStream() << "Could not write mat file "<< moutputfile;
		return 1;
	}
	return 0;

}

int transientPrepareInput(string moutputfile, string caseFolder, string casePrefix, SlowvarData slowvarData) {
	string indexerId = "transient";
	LOGGER->infoStream() << "Indexer name:  " << indexerId;

	std::valarray<double> *timesVect;
	vector< valarray<double>  >  *signalData;
	vector<double> *inertiaVect;

    bool enableFilter=false;
    string enableFilter_s = (const string) configFile.Value(indexerId,"filter","");
    if (enableFilter_s.compare("true") == 0) {
        enableFilter=true;
    }
    LOGGER->infoStream() << " filter enabled:  " << enableFilter;



    processEurostagResultsTransient(caseFolder.c_str(), casePrefix.c_str(), &timesVect, &signalData, &inertiaVect,enableFilter, slowvarData);
	if (verbose_flag) {
        string csvFilePath = moutputfile + "_"+ indexerId+".csv";
        dataAsCSV(*timesVect, *signalData, csvFilePath.c_str());
	}
	int timesVectSize=(*timesVect).size();
	if (timesVectSize == 0) {
		LOGGER->errorStream() << " no samples in Eurostag simulation results ";
		exit(-1);
	}
	LOGGER->infoStream() << " simulation index time window s_t=["  <<  (*timesVect)[0] << "," << (*timesVect)[timesVectSize-1] << "]";
	LOGGER->infoStream() << " simulation sampling size: " << timesVect->size();
	LOGGER->infoStream() << " inertia vector size:"  <<  inertiaVect->size();


	int err = 0, i;
	mat_t *mat;
	matvar_t *matvar;


    string matOutputfile=moutputfile + "_"+ indexerId+".mat";
    LOGGER->infoStream() << "writing mat file '"+  matOutputfile + "'";

    //mat = Mat_Open(fileName,MAT_ACC_RDWR);
	mat = Mat_CreateVer(matOutputfile.c_str(), NULL, mat_file_ver);

	if (mat) {


		// t
		int tsize=(*timesVect).size();

		size_t dims_t[1] = { tsize};
		double *t=new double[tsize];
		for (int i = 0; i < tsize; i++) {
			t[i] = (*timesVect)[i];
		}
		delete timesVect;

		matvar = Mat_VarCreate("t", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_t, t, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);
		delete t;


		// delta
		int S1size=signalData->size();
		size_t dims_S1[2] = {tsize, S1size};
		double *S1=new double[tsize*S1size];
		int i=0;
		for (int lineIndex = 0; lineIndex < S1size; ++lineIndex) {
			for (int timeIndex = 0; timeIndex < tsize; ++timeIndex) {
				S1[i]=((*signalData)[lineIndex])[timeIndex];
				i=i+1;
			}
		}
		matvar = Mat_VarCreate("delta", MAT_C_DOUBLE, MAT_T_DOUBLE, 2, dims_S1, S1, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);
		delete S1;


		// M
		int msize=inertiaVect->size();

		size_t dims_m[1] = { msize};
		double *m=new double[msize];
		for (int i = 0; i < msize; i++) {
			double genInertia=(*inertiaVect)[i];
			m[i] = genInertia*2.0;
		}
		delete inertiaVect;

		matvar = Mat_VarCreate("M", MAT_C_DOUBLE, MAT_T_DOUBLE, 1, dims_m, m, 0);
		Mat_VarWrite(mat, matvar, compression);
		Mat_VarFree(matvar);
		delete m;

		Mat_Close(mat);

		//free mem from function call
		delete signalData;
		LOGGER->infoStream() << " mat file '"+  moutputfile +  "', written.";

	} else {
		LOGGER->errorStream() << "Could not write mat file "<< moutputfile;
		return 1;
	}
	return 0;

}



int dispatchOnIndexid(set<string> IndexesNamesSet, std::string moutputfile, std::string caseFolder, std::string casePrefix, std::string configFileParam) {
    cout << "indexes size: "  << IndexesNamesSet.size() << endl;
    if (verbose_flag) {
        puts ("verbose flag is set");
    }

	/*
	 * handles init config file
	 */
	string configFilePath;
	char cwd[1024];
	getcwd(cwd, sizeof(cwd));
	string scwd=string(cwd);

	if (configFileParam.compare("") != 0) {
		if (is_file_exist(configFileParam.c_str())==false) {
			fprintf (stderr,"cannot find configuration file %s\n", configFileParam.c_str());
			exit(-1);
		}
		configFilePath=configFileParam;
	} else {
		//first, look for a config file in the current folder
		configFilePath=scwd+"/"+CONFIGFILENAME;
		if (is_file_exist(configFilePath.c_str())==false) {
			fprintf (stderr,"cannot find configuration file %s\n", CONFIGFILENAME.c_str());
			exit(-1);
		}
	}
	cout << "reading configuration from file: "<< configFilePath << endl;
	configFile=ConfigFile(configFilePath);

	/*
	 * setting a logger
	 */
	string logConfigFilePath;
	//if wp43adapterlog.properties is not found in ./
	//setup a basic logging, min cat INFO, only on STDOUT ..no more complex ways to retrieve configs, here:
	bool logpropsFileExists=true;
	logConfigFilePath=scwd+"/"+LOGCONFIGFILENAME;
	if (is_file_exist(logConfigFilePath.c_str())==false) {
		logpropsFileExists=false;
	}
	if (logpropsFileExists==true) {
		log4cpp::PropertyConfigurator::configure(logConfigFilePath);
	} else {
		log4cpp::BasicConfigurator::configure();
		log4cpp::PatternLayout* layout = new log4cpp::PatternLayout();
		layout->setConversionPattern("%d [%p] %m%n");
		log4cpp::Category::getRoot().getAppender()->setLayout(layout);
	}

	LOGGER = &log4cpp::Category::getRoot();

    string slowingVarFilePathS=string(caseFolder)+ "/" + string(casePrefix) +"_slowingvars.csv";
    SlowvarData slowData(slowingVarFilePathS);

    /*
     * exec adapter, dispatch on index id
     */
    set<string>::iterator itIndexId;
    for (itIndexId = IndexesNamesSet.begin(); itIndexId != IndexesNamesSet.end(); itIndexId++) {
        cout << "index: "<< *itIndexId << endl;

        if (itIndexId->compare("overload") == 0) {
            overloadPrepareInput(moutputfile, caseFolder, casePrefix, slowData);
        } else if (itIndexId->compare("underovervoltage") == 0) {
            underOverVoltagePrepareInput(moutputfile, caseFolder, casePrefix, slowData);
        } else if (itIndexId->compare("smallsignal") == 0) {
            smallSignalPrepareInput(moutputfile, caseFolder, casePrefix, slowData);
        } else if (itIndexId->compare("transient") == 0) {
            transientPrepareInput(moutputfile, caseFolder, casePrefix, slowData);
        } else {
            LOGGER->error("index " + *itIndexId + " not handled");
            exit(-1);
        }


    }


	/*
	 * shutdown logger
	 */
	log4cpp::Category::shutdown();
}



int main(int argc, char *argv[]) {
	string indexId;
	string moutputFile;
	string caseFolder;
	string casePrefix;
	string configFileParam;

    set<string> IndexesNamesSet;

	  int c;
	       while (1)
	         {
	           static struct option long_options[] =
	             {
	               /* These options set a flag. */
	               {"verbose", no_argument,       &verbose_flag, 1},
	               {"brief",   no_argument,       &verbose_flag, 0},
	               /* These options don't set a flag.
	                  We distinguish them by their indices. */
	               {"indexid",     no_argument,       0, 'a'},
	               {"moutput",  no_argument,       0, 'o'},
	               {"casefolder",     no_argument,       0, 'f'},
	               {"casename",  no_argument,       0, 'n'},
	               {"configfile",  no_argument,       0, 'c'},
	               {0, 0, 0, 0}
	             };
	           /* getopt_long stores the option index here. */
	           int option_index = 0;

	           c = getopt_long (argc, argv, "a:o:f:n:c:",
	                            long_options, &option_index);

	           /* Detect the end of the options. */
	           if (c == -1)
	             break;

	           switch (c)
	             {
	             case 0:
	               /* If this option set a flag, do nothing else now. */
	               if (long_options[option_index].flag != 0)
	                 break;
	               if (optarg) {
	                 //printf (" with arg %s\n", optarg);
	               }

	               break;

	             case 'a':
	               indexId=std::string(optarg);
                   IndexesNamesSet.insert(indexId);
	               break;

	             case 'o':
	               moutputFile=std::string(optarg);
	               break;

	             case 'f':
	               caseFolder=std::string(optarg);
	               break;

	             case 'n':
	               casePrefix=std::string(optarg);
	               break;


	             case 'c':
	            	 configFileParam=std::string(optarg);
	               break;

	             case '?':
	               /* getopt_long already printed an error message. */
	               break;

	             default:
	               abort ();
	             }
	         }


	       if (
	    		   (IndexesNamesSet.size() == 0)
	    		   || (moutputFile.compare("") == 0)
	    		   || (caseFolder.compare("") == 0)
	    		   || (casePrefix.compare("") == 0)
	       	   ) {
	    	   cerr << argv[0] << " [-a INDEXID] -o OUTPUTFILE -f EUROSTAG_CASE_FOLDER -n EUROSTAG_CASE_PREFIX [-c configfile]" << endl;
	    	   exit(-1);
	       }

	       // NOTE avoid exception handling (try&catch wrapper), for now: not knowing in detail what to expect, from this
	      dispatchOnIndexid(IndexesNamesSet, moutputFile, caseFolder, casePrefix, configFileParam);

 }


