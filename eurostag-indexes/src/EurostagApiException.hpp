// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file EurostagApiException.hpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#ifndef ITESLA_EUROSTAGAPIEXCEPTION_HPP
#define ITESLA_EUROSTAGAPIEXCEPTION_HPP

#include <stdexcept>
#include <boost/lexical_cast.hpp>
#include "TimeSeries.h"

namespace itesla {

    class EurostagApiException : public std::runtime_error {
    private:
        std::string _function;
        std::string _id;
        TimeSerieType _type;
        int _code;

    public:
        EurostagApiException(const std::string& function, const std::string& id, TimeSerieType type, int code)
                :  runtime_error(""),
                   _function(function),
                   _id(id),
                   _type(type),
                   _code(code) {
        }

        virtual ~EurostagApiException() throw() {
        }

        virtual const char* what() const throw() {
            return ("EurostagApiException(function=" + _function + ", id=" + _id + ", type=" + toStr(_type)
                    + ", code=" + boost::lexical_cast<std::string>(_code) + ")").c_str();
        }

    };

}



#endif //ITESLA_EUROSTAGAPIEXCEPTION_HPP
