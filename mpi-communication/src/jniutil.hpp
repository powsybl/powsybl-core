// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file jniutil.hpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#ifndef JNIUTIL_HPP
#define JNIUTIL_HPP

#include <cstring>
#include <string>
#include "jni.h"
#include "jniwrapper.hpp"

namespace powsybl {

namespace jni {

class ComPowsyblComputationMpiMpiTask : public JniWrapper<jobject> {
public:
    ComPowsyblComputationMpiMpiTask(JNIEnv* env, jobject obj)
        : JniWrapper<jobject>(env, obj) {
    }

    static void init(JNIEnv* env);

    int id() const;
    int rank() const;
    int thread() const;

    jbyteArray message() const;

    void resultMessage(const std::string& buffer);

private:
    static jmethodID _getIdMethodId;       
    static jmethodID _getRankMethodId;   
    static jmethodID _getThreadMethodId;   
    static jmethodID _getMessageMethodId;   
    static jmethodID _setResultMessageMethodId;   
};

}

}

#endif // JNIUTIL_HPP

