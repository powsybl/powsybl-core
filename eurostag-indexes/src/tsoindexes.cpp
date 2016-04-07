// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file tsoindexes.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <iostream>
#include <string>
#include <stdexcept>
#include <fstream>
#include <cmath>
#include <vector>
#include <set>
#include <map>
#include <boost/tokenizer.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/smart_ptr/shared_ptr.hpp>
#include <boost/algorithm/string/trim.hpp>
#include "EurostagTimeSeries.h"

const std::string TSO_OVERLOAD_LIMITS_CSV = "current_limits.csv";
const std::string TSO_VOLTAGE_LIMITS_CSV = "voltage_limits.csv";

const float SNREF = 100.; 
const float PMINI = 100.; // (MW)
const float ANGLE_MAX_SYNCH = 360.; // (deg)
const float FREQ_MAX_DEVIATION = 0.15; //(Hz)
const std::string EUSTAG_T = "eustag_t.e";
const float ANGLE_INVALID_VALUE = -1e+30;
const float FREQ_INVALID_VALUE = -5e+32;

/**
 * \class CurrentLimits
 * \brief Load current limits CSV
 */
struct CurrentLimits {
    std::string _lineId;
    float _limit1;
    float _limit2;
    float _nomV1;
    float _nomV2;
    std::string _iidmLineId;

    CurrentLimits(const std::string& lineId, float limit1, float limit2, float nomV1, float nomV2, const std::string& iidmLineId)
        : _lineId(lineId), _limit1(limit1), _limit2(limit2), _nomV1(nomV1), _nomV2(nomV2), _iidmLineId(iidmLineId) {}

    CurrentLimits(const CurrentLimits& other)
        : _lineId(other._lineId), _limit1(other._limit1), _limit2(other._limit2), _nomV1(other._nomV1), _nomV2(other._nomV2), _iidmLineId(other._iidmLineId) {}

    CurrentLimits& operator=(const CurrentLimits& other) {
        _lineId = other._lineId; 
        _limit1 = other._limit1;
        _limit2 = other._limit2;
        _nomV1 = other._nomV1;
        _nomV2 = other._nomV2;
        _iidmLineId = other._iidmLineId;
        return *this;
    }

    static void readCsv(const std::string& fileName, std::map<std::string, CurrentLimits>& limits) {
        std::ifstream ifs(fileName.c_str());
        if (!ifs)
           throw std::runtime_error("cannot open file " + fileName);

            std::string line;
            while (std::getline(ifs, line)) {
                boost::char_separator<char> sep(";");
                boost::tokenizer<boost::char_separator<char> > tokenizer(line, sep);
                std::vector<std::string> tokens(tokenizer.begin(), tokenizer.end());
                std::string lineId = tokens[0];
                float limit1 = boost::lexical_cast<float>(tokens[1]);
                float limit2 = boost::lexical_cast<float>(tokens[2]);
                float nomV1 = boost::lexical_cast<float>(tokens[3]);
                float nomV2 = boost::lexical_cast<float>(tokens[4]);
                std::string iidmLineId = tokens[5];
                limits.insert(std::make_pair(lineId, CurrentLimits(lineId, limit1, limit2, nomV1, nomV2, iidmLineId)));
            }
        if (!ifs.eof()) {
            throw std::runtime_error("error while reading line '" + line + "'");
        }
    }
};

/**
 * \class VoltageLimits
 * \brief Load voltage limits CSV
 */
struct VoltageLimits {
    std::string _busId;
    float _lowLimit;
    float _highLimit;
    float _nomV;

    VoltageLimits(const std::string& busId, float lowLimit, float highLimit, float nomV)
        : _busId(busId), _lowLimit(lowLimit), _highLimit(highLimit), _nomV(nomV) {}

    VoltageLimits(const VoltageLimits& other)
        : _busId(other._busId), _lowLimit(other._lowLimit), _highLimit(other._highLimit), _nomV(other._nomV) {}

    VoltageLimits& operator=(const VoltageLimits& other) {
        _busId = other._busId; 
        _lowLimit = other._lowLimit;
        _highLimit = other._highLimit;
        _nomV = other._nomV;
        return *this;
    }

    static void readCsv(const std::string& fileName, std::map<std::string, VoltageLimits>& limits) {
	std::ifstream ifs(fileName.c_str());
	if (!ifs) 
	   throw std::runtime_error("cannot open file " + fileName);
       
        std::string line;
        while (std::getline(ifs, line)) {
    	    boost::char_separator<char> sep(";");
    	    boost::tokenizer<boost::char_separator<char> > tokenizer(line, sep);
            std::vector<std::string> tokens(tokenizer.begin(), tokenizer.end());
            std::string busId = tokens[0];
            float lowLimit = boost::lexical_cast<float>(tokens[1]);
            float highLimit = boost::lexical_cast<float>(tokens[2]);
            float nomV = boost::lexical_cast<float>(tokens[3]);
            limits.insert(std::make_pair(busId, VoltageLimits(busId, lowLimit, highLimit, nomV)));
        }
        if (!ifs.eof()) {
            throw std::runtime_error("error while reading line '" + line + "'");
        }
    }
};

void readDictionaryCsv(const std::string& fileName, std::map<std::string, std::string>& dictionary) {
    std::ifstream ifs(fileName.c_str());
    if (!ifs)
        throw std::runtime_error("cannot open file " + fileName);

    std::string line;
    while (std::getline(ifs, line)) {
        boost::char_separator<char> sep(";");
        boost::tokenizer<boost::char_separator<char> > tokenizer(line, sep);
        std::vector<std::string> tokens(tokenizer.begin(), tokenizer.end());
        std::string id1 = tokens[0];
        std::string id2 = tokens[1];
        dictionary.insert(std::make_pair(id2, id1));
    }
    if (!ifs.eof()) {
        throw std::runtime_error("error while reading line '" + line + "'");
    }
}

enum GtbVariable {
    PN,
    IB,
    U
};

void writeGtb(const std::string& fileName, const std::string& outputFileName, const std::string& title, const std::string& listVar, GtbVariable variable) {
    std::ofstream ofs(fileName.c_str());
    if (!ofs)
        throw std::runtime_error("cannot open file " + fileName);
    ofs << "HEADER     21/01/14 5.1" << std::endl
    << "* ---------------------------------------------" << std::endl
    << "OBSERVABLE_DEFINITION" << std::endl
    << "* ---------------------------------------------" << std::endl
    << "BEGIN" << std::endl
    << "TIME = TFIN" << std::endl
    << std::endl
    << std::endl
    << "FILENAME.OUTPUT = \"" << outputFileName << "\"" << std::endl
    << std::endl
    << "* ---------------------------------------------" << std::endl
    << "LIST_DEFINITION" << std::endl
    << "* ---------------------------------------------" << std::endl
    << "TABLE_DEFINITION" << std::endl
    << "TITLE \"" << title << "\"" << std::endl
    << listVar << " -> ";
    switch (variable) {
        case PN:
            ofs << "PN";
            break;
        case IB:
            ofs << "IB";
            break;
        case U:
            ofs << "U";
            break;
        default:
            throw std::runtime_error("Unknown variable");
    }
    ofs << std::endl
    << "LEGEND \"\"" << std::endl
    << std::endl
    << "* ---------------------------------------------" << std::endl
    << "CURVE_DEFINITION" << std::endl
    << "END" << std::endl;
}

struct ComputationContext {

    std::string _workingDir;
    std::string _faultId;
    const itesla::TimeSeries& _timeSeries;

    ComputationContext(const std::string& workingDir, const std::string& faultId, const itesla::TimeSeries& timeSeries)
            : _workingDir(workingDir),
              _faultId(faultId),
              _timeSeries(timeSeries) {
    }
};

void readMachinesNominalPower(const ComputationContext& context, std::map<std::string, float> &machines) {
    //Use the GTB to list the generators: use eustag_g to generate [gen_name];[Pnom] map. (could be replaced by a given file in input)
    writeGtb(context._workingDir + "/" + context._faultId + "_S.gtb", context._faultId + "_S", "grp", "GENERATOR_LIST", PN);
    std::string myCommand = "cd \"" + context._workingDir + "\" ;" + EUSTAG_T + " " + context._faultId + "_S.gtb " + context._faultId + ".res";
    if (system(myCommand.c_str()) != 0)
        throw std::runtime_error(EUSTAG_T + " error");

    // parse the generated file eurostag_t.t2n and check limits
    std::string t2nFilePathS = context._workingDir + "/" + context._faultId + "_S.t2n";
    std::ifstream it2n(t2nFilePathS.c_str());
    if (!it2n)
        throw std::runtime_error("cannot open file " + t2nFilePathS);

    std::string line;

    // skip header (3 first lines)
    std::getline(it2n, line);
    std::getline(it2n, line);
    std::getline(it2n, line);

    // effective parsing
    boost::char_separator<char> sep(";");
    while (std::getline(it2n, line)) {
        boost::algorithm::trim(line);
        if (line.empty()) {
            continue;
        }

        boost::tokenizer<boost::char_separator<char> > tokenizer(line, sep);
        std::vector<std::string> tokens(tokenizer.begin(), tokenizer.end());

        if (tokens[0] == "GENERATOR_LIST" && tokens[1] == "PN") {
            continue;
        } else if ((tokens[0] == "LINE_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "TF_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "ULTC_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "COUPLINGDVC_LIST" && tokens[1] == "_CBS_")) {
            break;
        } else {
            const std::string& geneId = tokens[0];
            float Pnom = boost::lexical_cast<float>(tokens[1]); // Nominal power of the generating unit

            machines[geneId] = Pnom;
        }
    }
}

void readLinesFlow(const ComputationContext &context, std::map<std::string, float>& lines) {
    // use eustag_g to generate [line_name];[current] file.
    writeGtb(context._workingDir + "/" + context._faultId + "_I.gtb", context._faultId + "_I", "JBT", "LINE_LIST", IB);
    std::string myCommand = "cd \"" + context._workingDir + "\" ;" + EUSTAG_T + " " + context._faultId + "_I.gtb " + context._faultId + ".res";
    if (system(myCommand.c_str()) != 0)
        throw std::runtime_error(EUSTAG_T + " error");

    // parse the generated file eurostag_t.t2n and check limits
    std::string t2nFilePathS = context._workingDir + "/" + context._faultId + "_I.t2n";
    std::ifstream it2n(t2nFilePathS.c_str());
    if (!it2n)
        throw std::runtime_error("cannot open file " + t2nFilePathS);

    std::string line;

    // skip header (3 first lines)
    std::getline(it2n, line);
    std::getline(it2n, line);
    std::getline(it2n, line);

    // effective parsing
    boost::char_separator<char> sep(";");
    while (std::getline(it2n, line)) {
        boost::algorithm::trim(line);
        if (line.empty()) {
            continue;
        }

        boost::tokenizer<boost::char_separator<char> > tokenizer(line, sep);
        std::vector<std::string> tokens(tokenizer.begin(), tokenizer.end());

        if (tokens[0] == "LINE_LIST" && tokens[1] == "IB") {
            continue;
        } else if ((tokens[0] == "LINE_LIST" && tokens[1] == "IB")
                   || (tokens[0] == "LINE_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "TF_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "ULTC_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "COUPLINGDVC_LIST" && tokens[1] == "_CBS_")) {
            break;
        } else {
            const std::string& lineId = tokens[0];
            float puLineCurrent = boost::lexical_cast<float>(tokens[1]); // current of the line in pu: base = SNREF/sqrt(3)*(base voltage at node)

            lines[lineId] = puLineCurrent;
        }
    }
}

void readBusesVoltage(const ComputationContext& context, std::map<std::string, float>& buses) {
    // use eustag_g to generate [node_name];[V (pu)] file.
    writeGtb(context._workingDir + "/" + context._faultId + "_V.gtb", context._faultId + "_V", "GTB_V", "NODE_LIST", U);
    std::string myCommand = "cd \"" + context._workingDir + "\" ;" + EUSTAG_T + " " + context._faultId + "_V.gtb " + context._faultId + ".res";
    system(myCommand.c_str());
    if (system(myCommand.c_str()) != 0)
        throw std::runtime_error(EUSTAG_T + " error");

    // parse the generated file eurostag_t.t2n and check limits
    std::string t2nFilePathS = context._workingDir + "/" + context._faultId + "_V.t2n";
    std::ifstream it2n(t2nFilePathS.c_str());
    if (!it2n)
        throw std::runtime_error("cannot open file " + t2nFilePathS);

    std::string line;

    // skip header (3 first lines)
    std::getline(it2n, line);
    std::getline(it2n, line);
    std::getline(it2n, line);

    // effective parsing
    boost::char_separator<char> sep(";");
    while (std::getline(it2n, line)) {
        boost::algorithm::trim(line);
        if (line.empty()) {
            continue;
        }

        boost::tokenizer<boost::char_separator<char> > tokenizer(line, sep);
        std::vector<std::string> tokens(tokenizer.begin(), tokenizer.end());

        if (tokens[0] == "NODE_LIST" && tokens[1] == "U") {
            continue;
        } else if ((tokens[0] == "LINE_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "TF_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "ULTC_LIST" && tokens[1] == "_CBS_")
                   || (tokens[0] == "COUPLINGDVC_LIST" && tokens[1] == "_CBS_")) {
            break;
        } else {
            const std::string& busId = tokens[0];
            float puNodeVoltage = boost::lexical_cast<float>(tokens[1]); // voltage in pu

            buses[busId] = puNodeVoltage;
        }
    }
}

class TsoIndex {

public:
    virtual ~TsoIndex() {}
    virtual void print() const = 0;
    virtual void writeXml(std::ofstream& ofs) const = 0;
};

/**
 * \class TsoFrequencyIndex
 * \brief Index for checking values of Frequency during a complete simulation
 */
struct TsoFrequencyIndex : public TsoIndex {
    TsoFrequencyIndex()
        : _freqOutCount(0),
          _nbNodesOutstandingFreq(0) {
    }

    int _freqOutCount; //nb nodes with non acceptable frequency
    int _nbNodesOutstandingFreq; //nb nodes with abnormal frequency (if high, probably a split of the grid)

    virtual void print() const;
    virtual void writeXml(std::ofstream& ofs) const;

    static boost::shared_ptr<TsoFrequencyIndex> compute(const ComputationContext& context);
};

void TsoFrequencyIndex::print() const {
    std::cout << "freqOutCount = " << _freqOutCount << ", nbNodesOutstandingFreq = " << _nbNodesOutstandingFreq << std::endl;
}

void TsoFrequencyIndex::writeXml(std::ofstream& ofs) const {
    ofs << "    <index name=\"tso-frequency\">\n"
        << "        <freq-out-count>" << _freqOutCount << "</freq-out-count>\n"
        << "    </index>\n";
}

boost::shared_ptr<TsoFrequencyIndex> TsoFrequencyIndex::compute(const ComputationContext& context) {

    boost::shared_ptr<TsoFrequencyIndex> index(new TsoFrequencyIndex());

    // Use of the list of nodes from the voltageLimits.csv file
    std::map<std::string, VoltageLimits> limitsMap;
    VoltageLimits::readCsv(context._workingDir + "/" + TSO_VOLTAGE_LIMITS_CSV, limitsMap);

    for (std::map<std::string, VoltageLimits>::const_iterator it = limitsMap.begin(); it != limitsMap.end(); it++) {
        const VoltageLimits& limits = it->second;
        const std::string& nodeName = it->first;
        float nodeNomVoltage = limits._nomV;

        //bypass lower voltage levels, frequency screened only at EHV, to avoid local pbs
        if (nodeNomVoltage <= 300. ) 
            continue;

        //std::cout << "node = " << nodeName << " Vnom = " << nodeNomVoltage << std::endl;

        std::vector<float> values = context._timeSeries.read(nodeName, itesla::TimeSerieType::FREQUENCY);

        float freqMin = NAN;
        float freqMax = NAN;
        for (size_t i = 0; i < values.size(); ++i) {
            float freq = values[i];
            if (freq != FREQ_INVALID_VALUE) { // disconnected nodes
                freqMin = isnan(freqMin) ? freq : std::min(freq, freqMin);
                freqMax = isnan(freqMax) ? freq : std::max(freq, freqMax);   
            } 
        }

        //potentially disconnected nodes, bypassing
        if (freqMax == 0. || freqMin  == 0.)
            continue;

        //nodes probably disconnected from main synchronous area, bypassing but keeping the info
        if (freqMax > 55. || freqMin < 45.){
            index->_nbNodesOutstandingFreq++;
            continue;
        }

        //real nodes of interest
        float FMAX = 50. + FREQ_MAX_DEVIATION;
        float FMIN = 50. - FREQ_MAX_DEVIATION;
        if (freqMax > FMAX || freqMin < FMIN) {
            index->_freqOutCount++;
            std::cout << "bus " << nodeName << " has an unacceptable frequency" << std::endl;
        }
    }

    index->print();

    return index;
}

/**
 * \class TsoSynchroLossIndex
 * \brief Index for checking values of rotor Angle during a complete simulation
 */
struct TsoSynchroLossIndex : public TsoIndex {
    TsoSynchroLossIndex()
        : _desynchronizedGenerators()
    {}

    std::map<std::string, float> _desynchronizedGenerators;
    
    virtual void print() const;
    virtual void writeXml(std::ofstream& ofs) const;

    static boost::shared_ptr<TsoSynchroLossIndex> compute(const ComputationContext& context, const std::map<std::string, float>& machines, const std::map<std::string, std::string>& dictionary);
};

void TsoSynchroLossIndex::print() const {
    std::cout << "desynchronizedGenerators = [";
    for (std::map<std::string, float>::const_iterator it = _desynchronizedGenerators.begin(); it != _desynchronizedGenerators.end(); it++) {
        std::cout << it->first << "=" << it->second;
        if (it != _desynchronizedGenerators.end()) {
            std::cout << ", ";
        }
    }
    std::cout << "]" << std::endl;
}

void TsoSynchroLossIndex::writeXml(std::ofstream& ofs) const {
    ofs << "    <index name=\"tso-synchro-loss\">\n"
        << "        <synchro-loss-count>" << _desynchronizedGenerators.size() << "</synchro-loss-count>\n";
    if (_desynchronizedGenerators.size() > 0) {
        for (std::map<std::string, float>::const_iterator it = _desynchronizedGenerators.begin(); it != _desynchronizedGenerators.end(); it++) {
            ofs << "        <generator id=\"" << it->first << "\">" << it->second << "</generator>\n";
        }
    }
    ofs << "    </index>\n";
}

boost::shared_ptr<TsoSynchroLossIndex> TsoSynchroLossIndex::compute(const ComputationContext& context,
                                                                    const std::map<std::string, float>& machines,
                                                                    const std::map<std::string, std::string>& dictionary) {

    boost::shared_ptr<TsoSynchroLossIndex> index(new TsoSynchroLossIndex());

    //Loop on a generating units of the GTB to extract rotor angle time series in the .res file
    for (std::map<std::string, float>::const_iterator it = machines.begin(); it != machines.end(); it++) {
        const std::string& esgId = it->first;
        float genPnom = it->second;

        std::map<std::string, std::string>::const_iterator itD =  dictionary.find(esgId);
        if (itD == dictionary.end()) {
            throw std::runtime_error("Generator " + esgId + " not found in the dictionary");
        }
        const std::string& iidmId = itD->second;

        //bypass small machines
        if (genPnom < PMINI)
           continue;

        std::vector<float> values = context._timeSeries.read(esgId, itesla::TimeSerieType::ANGLE);

        float angleMin = NAN;
        float angleMax = NAN;
        for (size_t i = 0; i < values.size(); ++i) {
            float angle = values[i];
            if (angle != ANGLE_INVALID_VALUE) { // to detect disconnected machines
                angleMin = isnan(angleMin) ? angle : std::min(angleMin, angle);
                angleMax = isnan(angleMax) ? angle : std::max(angleMax, angle);               
            }
        }

        if (std::abs(angleMax - angleMin) * 180 / M_PI > ANGLE_MAX_SYNCH){
            index->_desynchronizedGenerators[iidmId] = genPnom;
            std::cout << "generator " << iidmId << " lost synchro" << std::endl;
        }
    }

    index->print();

    return index;
}


/**
 * \class TsoOverloadIndex
 * \brief Index for comparing loads of the lines with overload limits defined by TSOs
 */
class TsoOverloadIndex : public TsoIndex {
public:
    TsoOverloadIndex()
        : _overloadedBranches()
    {}

    std::set<std::string> _overloadedBranches;

    virtual void print() const;
    virtual void writeXml(std::ofstream& ofs) const;

    static boost::shared_ptr<TsoOverloadIndex> compute(const ComputationContext& context);
};

void TsoOverloadIndex::print() const {
    std::cout << "overloadCount = " << _overloadedBranches.size() << std::endl;
}

void TsoOverloadIndex::writeXml(std::ofstream& ofs) const {
    ofs << "    <index name=\"tso-overload\">\n";
    for (std::set<std::string>::const_iterator it = _overloadedBranches.begin(); it != _overloadedBranches.end(); it++) {
        ofs << "        <overloaded-branch>" << *it << "</overloaded-branch>\n";
    }
    ofs << "        <overload-count>" << _overloadedBranches.size() << "</overload-count>\n"
        << "    </index>\n";
}

boost::shared_ptr<TsoOverloadIndex> TsoOverloadIndex::compute(const ComputationContext& context) {

    boost::shared_ptr<TsoOverloadIndex> index(new TsoOverloadIndex());

    // load limits
    std::map<std::string, CurrentLimits> limitsMap;
    CurrentLimits::readCsv(context._workingDir + "/" + TSO_OVERLOAD_LIMITS_CSV, limitsMap);

    std::map<std::string, float> lines;
    readLinesFlow(context, lines);

    for (std::map<std::string, float>::const_iterator itL = lines.begin(); itL != lines.end(); itL++) {
        const std::string& lineId = itL->first;
        float puLineCurrent = itL->second; // current of the line in pu: base = SNREF/sqrt(3)*(base voltage at node)

        std::map<std::string, CurrentLimits>::const_iterator it = limitsMap.find(lineId);
        if (it == limitsMap.end()) {
            // also check with the invert
            std::string lineId2 = lineId.substr(9, 8) + '-' + lineId.substr(0, 8) + '-' + lineId[18];
            it = limitsMap.find(lineId2);
            if (it == limitsMap.end()) {
                std::cerr << "limit no found for line " << lineId << "/" << lineId2 << std::endl;
                continue;
            }
        }

        const CurrentLimits& limits = it->second;

        // get current in (A)
        float lineCurrent = puLineCurrent * SNREF * 1000. / (limits._nomV1 * sqrt(3)); // number 1000 is for (kA) to (A)

        if (std::abs(lineCurrent) > limits._limit1) {
            std::cerr << "line " << lineId << " (" << limits._iidmLineId << ") is over limit (" << std::abs(lineCurrent) << " > " << limits._limit1 << ")" << std::endl;
            index->_overloadedBranches.insert(limits._iidmLineId);
        }
    }

    index->print();

    return index;
}

/**
 * \class TsoVoltageLimitIndex
 * \brief Index for comparing voltages of the nodes with voltage limits defined by TSOs
 */
class TsoVoltageLimitIndex : public TsoIndex {
public:
    TsoVoltageLimitIndex()
        : _undervoltageCount(0),
          _overvoltageCount(0)
    {}

    int _undervoltageCount;
    int _overvoltageCount;

    virtual void print() const;
    virtual void writeXml(std::ofstream& ofs) const;

    static boost::shared_ptr<TsoVoltageLimitIndex> compute(const ComputationContext& context);
};

void TsoVoltageLimitIndex::print() const {
    std::cout << "undervoltageCount = " << _undervoltageCount << std::endl;
    std::cout << "overvoltageCount = " << _overvoltageCount << std::endl;
}

void TsoVoltageLimitIndex::writeXml(std::ofstream& ofs) const {
    ofs << "    <index name=\"tso-undervoltage\">\n"
        << "        <undervoltage-count>" << _undervoltageCount << "</undervoltage-count>\n"
        << "    </index>\n"
        << "    <index name=\"tso-overvoltage\">\n"
        << "        <overvoltage-count>" << _overvoltageCount << "</overvoltage-count>\n"
        << "    </index>\n";
}

boost::shared_ptr<TsoVoltageLimitIndex> TsoVoltageLimitIndex::compute(const ComputationContext& context) {
    boost::shared_ptr<TsoVoltageLimitIndex> index(new TsoVoltageLimitIndex());

    // load limits from csv
    std::map<std::string, VoltageLimits> limitsMap;
    VoltageLimits::readCsv(context._workingDir + "/" + TSO_VOLTAGE_LIMITS_CSV, limitsMap);

    std::map<std::string, float> buses;
    readBusesVoltage(context, buses);

    for (std::map<std::string, float>::const_iterator itB = buses.begin(); itB != buses.end(); itB++) {
        const std::string& busId = itB->first;
        float puNodeVoltage = itB->second; // voltage in pu

        std::map<std::string, VoltageLimits>::const_iterator it = limitsMap.find(busId);
        if (it == limitsMap.end()) {
            std::cerr << "limit no found for bus " << busId << std::endl;
            continue;
        }

        const VoltageLimits& limits = it->second;

        if (puNodeVoltage < 0.0001 || puNodeVoltage > 999.) //rapid test on non-interesting values
            continue;

        // get Voltage in (kV)
        float nodeVoltage =  puNodeVoltage * limits._nomV;

        // check versus voltage limits
        if (limits._lowLimit > nodeVoltage) {
            std::cerr << "bus " << busId << " is under limit (" << nodeVoltage << " < " << limits._lowLimit << ")" << std::endl;
            index->_undervoltageCount++;
        }
        if (limits._highLimit < nodeVoltage) {
            std::cerr << "bus " << busId << " is over limit (" << nodeVoltage << " > " << limits._highLimit << ")" << std::endl;
            index->_overvoltageCount++;
        }
    }

    return index;
}

class TsoGeneratorVoltageAutomatonIndex : public TsoIndex {
public:
    TsoGeneratorVoltageAutomatonIndex()
    {}

    std::vector<std::string> onUnderVoltageDisconnectedGenerators;
    std::vector<std::string> onOverVoltageDisconnectedGenerators;

    virtual void print() const;
    virtual void writeXml(std::ofstream& ofs) const;

    static boost::shared_ptr<TsoGeneratorVoltageAutomatonIndex> compute(const ComputationContext& context, const std::map<std::string, float>& machines, const std::map<std::string, std::string>& dictionary);
};

void TsoGeneratorVoltageAutomatonIndex::print() const {
    std::cout << "onUnderVoltageDisconnectedGenerators = [";
    for (std::vector<std::string>::const_iterator it = onUnderVoltageDisconnectedGenerators.begin(); it != onUnderVoltageDisconnectedGenerators.end(); it++) {
        std::cout << *it;
        if (it != onUnderVoltageDisconnectedGenerators.end()) {
            std::cout << ", ";
        }
    }
    std::cout << "]" << std::endl;
    std::cout << "onOverVoltageDisconnectedGenerators = [";
    for (std::vector<std::string>::const_iterator it = onOverVoltageDisconnectedGenerators.begin(); it != onOverVoltageDisconnectedGenerators.end(); it++) {
        std::cout << *it << " ";
        if (it != onOverVoltageDisconnectedGenerators.end()) {
            std::cout << ", ";
        }
    }
    std::cout << "]" << std::endl;
}

void TsoGeneratorVoltageAutomatonIndex::writeXml(std::ofstream& ofs) const {
    ofs << "    <index name=\"tso-generator-voltage-automaton\">\n";
    if (onUnderVoltageDisconnectedGenerators.size() > 0) {
        ofs << "        <onUnderVoltageDisconnectedGenerators>\n";
        for (std::vector<std::string>::const_iterator it = onUnderVoltageDisconnectedGenerators.begin();
             it != onUnderVoltageDisconnectedGenerators.end(); it++) {
            ofs << "            <gen>" << *it << "</gen>\n";
        }
        ofs << "        </onUnderVoltageDisconnectedGenerators>\n";
    }
    if (onOverVoltageDisconnectedGenerators.size() > 0) {
        ofs << "        <onOverVoltageDisconnectedGenerators>\n";
        for (std::vector<std::string>::const_iterator it = onOverVoltageDisconnectedGenerators.begin();
             it != onOverVoltageDisconnectedGenerators.end(); it++) {
            ofs << "            <gen>" << *it << "</gen>\n";
        }
        ofs << "        </onOverVoltageDisconnectedGenerators>\n";
    }
    ofs << "    </index>\n";
}

boost::shared_ptr<TsoGeneratorVoltageAutomatonIndex> TsoGeneratorVoltageAutomatonIndex::compute(const ComputationContext& context,
                                                                                                const std::map<std::string, float>& machines,
                                                                                                const std::map<std::string, std::string>& dictionary) {

    boost::shared_ptr<TsoGeneratorVoltageAutomatonIndex> index(new TsoGeneratorVoltageAutomatonIndex());

    for (std::map<std::string, float>::const_iterator it = machines.begin(); it != machines.end(); it++) {
        const std::string& esgId = it->first;

        std::map<std::string, std::string>::const_iterator itD =  dictionary.find(esgId);
        if (itD == dictionary.end()) {
            throw std::runtime_error("Generator " + esgId + " not found in the dictionary");
        }
        const std::string& iidmId = itD->second;

        // see puauto11.ccd for automaton state documentation

        std::vector<float> values = context._timeSeries.read(esgId, itesla::TimeSerieType::UNDER_VOLTAGE_AUTOMATON);

        for (size_t i = 0; i < values.size(); ++i) {
            float v = values[i];
            if (v == -1) {
                index->onUnderVoltageDisconnectedGenerators.push_back(iidmId);
                break;
            }
        }

        values = context._timeSeries.read(esgId, itesla::TimeSerieType::OVER_VOLTAGE_AUTOMATON);

        for (size_t i = 0; i < values.size(); ++i) {
            float v = values[i];
            if (v == -1) {
                index->onOverVoltageDisconnectedGenerators.push_back(iidmId);
                break;
            }
        }
    }

    index->print();

    return index;
}

class TsoGeneratorSpeedAutomatonIndex : public TsoIndex {
public:
    TsoGeneratorSpeedAutomatonIndex()
    {}

    std::vector<std::string> onUnderSpeedDisconnectedGenerators;
    std::vector<std::string> onOverSpeedDisconnectedGenerators;

    virtual void print() const;
    virtual void writeXml(std::ofstream& ofs) const;

    static boost::shared_ptr<TsoGeneratorSpeedAutomatonIndex> compute(const ComputationContext& context, const std::map<std::string, float>& machines, const std::map<std::string, std::string>& dictionary);
};

void TsoGeneratorSpeedAutomatonIndex::print() const {
    std::cout << "onUnderSpeedDisconnectedGenerators = [";
    for (std::vector<std::string>::const_iterator it = onUnderSpeedDisconnectedGenerators.begin(); it != onUnderSpeedDisconnectedGenerators.end(); it++) {
        std::cout << *it;
        if (it != onUnderSpeedDisconnectedGenerators.end()) {
            std::cout << ", ";
        }
    }
    std::cout << "]" << std::endl;
    std::cout << "onOverSpeedDisconnectedGenerators = [";
    for (std::vector<std::string>::const_iterator it = onOverSpeedDisconnectedGenerators.begin(); it != onOverSpeedDisconnectedGenerators.end(); it++) {
        std::cout << *it;
        if (it != onOverSpeedDisconnectedGenerators.end()) {
            std::cout << ", ";
        }
    }
    std::cout << "]" << std::endl;
}

void TsoGeneratorSpeedAutomatonIndex::writeXml(std::ofstream& ofs) const {
    ofs << "    <index name=\"tso-generator-speed-automaton\">\n";
    if (onUnderSpeedDisconnectedGenerators.size() > 0) {
        ofs << "        <onUnderSpeedDisconnectedGenerators>\n";
        for (std::vector<std::string>::const_iterator it = onUnderSpeedDisconnectedGenerators.begin();
             it != onUnderSpeedDisconnectedGenerators.end(); it++) {
            ofs << "            <gen>" << *it << "</gen>\n";
        }
        ofs << "        </onUnderSpeedDisconnectedGenerators>\n";
    }
    if (onOverSpeedDisconnectedGenerators.size() > 0) {
        ofs << "        <onOverSpeedDisconnectedGenerators>\n";
        for (std::vector<std::string>::const_iterator it = onOverSpeedDisconnectedGenerators.begin();
             it != onOverSpeedDisconnectedGenerators.end(); it++) {
            ofs << "            <gen>" << *it << "</gen>\n";
        }
        ofs << "        </onOverSpeedDisconnectedGenerators>\n";
    }
    ofs << "    </index>\n";
}

boost::shared_ptr<TsoGeneratorSpeedAutomatonIndex> TsoGeneratorSpeedAutomatonIndex::compute(const ComputationContext& context,
                                                                                            const std::map<std::string, float>& machines,
                                                                                            const std::map<std::string, std::string>& dictionary) {

    boost::shared_ptr<TsoGeneratorSpeedAutomatonIndex> index(new TsoGeneratorSpeedAutomatonIndex());

    for (std::map<std::string, float>::const_iterator it = machines.begin(); it != machines.end(); it++) {
        const std::string& esgId = it->first;

        std::map<std::string, std::string>::const_iterator itD =  dictionary.find(esgId);
        if (itD == dictionary.end()) {
            throw std::runtime_error("Generator " + esgId + " not found in the dictionary");
        }
        const std::string& iidmId = itD->second;

        // see puauto12.ccd for automaton state documentation

        std::vector<float> values = context._timeSeries.read(esgId, itesla::TimeSerieType::UNDER_SPEED_AUTOMATON);

        for (size_t i = 0; i < values.size(); ++i) {
            float v = values[i];
            if (v == -1) {
                index->onUnderSpeedDisconnectedGenerators.push_back(iidmId);
                break;
            }
        }

        values = context._timeSeries.read(esgId, itesla::TimeSerieType::OVER_SPEED_AUTOMATON);

        for (size_t i = 0; i < values.size(); ++i) {
            float v = values[i];
            if (v == -1) {
                index->onOverSpeedDisconnectedGenerators.push_back(iidmId);
                break;
            }
        }
    }

    index->print();

    return index;
}

class TsoGeneratorDisconnectionIndex : public TsoIndex {
public:
    TsoGeneratorDisconnectionIndex()
    {}

    std::map<std::string, float> _disconnectedGenerators;

    virtual void print() const;
    virtual void writeXml(std::ofstream& ofs) const;

    static boost::shared_ptr<TsoGeneratorDisconnectionIndex> compute(const ComputationContext& context, const std::map<std::string, float>& machines, const std::map<std::string, std::string>& dictionary);
};

void TsoGeneratorDisconnectionIndex::print() const {
    std::cout << "disconnectedGenerators = [";
    for (std::map<std::string, float>::const_iterator it = _disconnectedGenerators.begin(); it != _disconnectedGenerators.end(); it++) {
        std::cout << it->first << "=" << it->second;
        if (it != _disconnectedGenerators.end()) {
            std::cout << ", ";
        }
    }
    std::cout << "]" << std::endl;
}

void TsoGeneratorDisconnectionIndex::writeXml(std::ofstream& ofs) const {
    ofs << "    <index name=\"tso-disconnected-generator\">\n";
    if (_disconnectedGenerators.size() > 0) {
        for (std::map<std::string, float>::const_iterator it = _disconnectedGenerators.begin(); it != _disconnectedGenerators.end(); it++) {
            ofs << "        <generator id=\"" << it->first << "\">" << it->second << "</generator>\n";
        }
    }
    ofs << "    </index>\n";
}

boost::shared_ptr<TsoGeneratorDisconnectionIndex> TsoGeneratorDisconnectionIndex::compute(const ComputationContext& context,
                                                                                          const std::map<std::string, float>& machines,
                                                                                          const std::map<std::string, std::string>& dictionary) {

    boost::shared_ptr<TsoGeneratorDisconnectionIndex> index(new TsoGeneratorDisconnectionIndex());

    for (std::map<std::string, float>::const_iterator it = machines.begin(); it != machines.end(); it++) {
        const std::string& esgId = it->first;

        std::map<std::string, std::string>::const_iterator itD =  dictionary.find(esgId);
        if (itD == dictionary.end()) {
            throw std::runtime_error("Generator " + esgId + " not found in the dictionary");
        }
        const std::string& iidmId = itD->second;

        std::vector<float> values = context._timeSeries.read(esgId, itesla::TimeSerieType::GENERATOR_ACTIVE_POWER);

        if (values[0] != ANGLE_INVALID_VALUE && values[values.size()-1] == ANGLE_INVALID_VALUE) {
            index->_disconnectedGenerators[iidmId] = values[0];
        }
    }

    index->print();

    return index;
}

void writeXml(const std::string& workingDir, const std::string& faultId, const std::vector<boost::shared_ptr<TsoIndex> >& indexes) {
    std::string fileName = workingDir + "/" + faultId + "_tso_limits_security_indexes.xml";
    std::ofstream ofs(fileName.c_str());
    if (!ofs)
        throw std::runtime_error("cannot open file " + fileName);
    ofs << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        << "<indexes>\n";
    for (std::vector<boost::shared_ptr<TsoIndex> >::const_iterator it = indexes.begin(); it != indexes.end(); it++) {
        (*it)->writeXml(ofs);
    }
    ofs << "</indexes>\n";
}

int main (int argc, char *argv[]) {
    if (argc != 3) {
       std::cerr << argv[0] << " <working-dir> <fault-id>" << std::endl;
       exit(-1);
    }
    try {
        std::string workingDir = argv[1];
        std::string faultId = argv[2];

        itesla::EurostagTimeSeries timeSeries(workingDir + "/" + faultId);

        ComputationContext context(workingDir, faultId, timeSeries);

        std::map<std::string, float> machines;
        readMachinesNominalPower(context, machines);

        std::map<std::string, std::string> dictionary;
        readDictionaryCsv(workingDir + "/" + "dict_gens.csv", dictionary);

        boost::shared_ptr<TsoOverloadIndex> overloadIndex = TsoOverloadIndex::compute(context);
        boost::shared_ptr<TsoVoltageLimitIndex> voltageLimitIndex = TsoVoltageLimitIndex::compute(context);

        boost::shared_ptr<TsoSynchroLossIndex> synchroLossIndex = TsoSynchroLossIndex::compute(context, machines, dictionary);
        boost::shared_ptr<TsoFrequencyIndex> frequencyIndex = TsoFrequencyIndex::compute(context);
        boost::shared_ptr<TsoGeneratorVoltageAutomatonIndex> generatorVoltageAutomatonIndex = TsoGeneratorVoltageAutomatonIndex::compute(
                context, machines, dictionary);
        boost::shared_ptr<TsoGeneratorSpeedAutomatonIndex> generatorSpeedAutomatonIndex = TsoGeneratorSpeedAutomatonIndex::compute(
                context, machines, dictionary);
        boost::shared_ptr<TsoGeneratorDisconnectionIndex> generatorDisconnectionIndex = TsoGeneratorDisconnectionIndex::compute(
                context, machines, dictionary);

        std::vector<boost::shared_ptr<TsoIndex> > indexes;
        indexes.push_back(overloadIndex);
        indexes.push_back(voltageLimitIndex);
        indexes.push_back(synchroLossIndex);
        indexes.push_back(frequencyIndex);
        indexes.push_back(generatorVoltageAutomatonIndex);
        indexes.push_back(generatorSpeedAutomatonIndex);
        indexes.push_back(generatorDisconnectionIndex);
        writeXml(workingDir, faultId, indexes);

        return 0;
    } catch (const std::exception& e) {
        std::cerr << e.what() << std::endl;
        return -1;
    } catch (...) {
        std::cerr << "unknown exception" << std::endl;
        return -2;
    }

}


