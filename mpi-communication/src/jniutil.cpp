// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file jniutil.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include "jniutil.hpp"

namespace powsybl {

namespace jni {

jmethodID ComPowsyblComputationMpiMpiTask::_getIdMethodId = 0;   
jmethodID ComPowsyblComputationMpiMpiTask::_getRankMethodId = 0;   
jmethodID ComPowsyblComputationMpiMpiTask::_getThreadMethodId = 0;
jmethodID ComPowsyblComputationMpiMpiTask::_getMessageMethodId = 0;   
jmethodID ComPowsyblComputationMpiMpiTask::_setResultMessageMethodId = 0;   

void ComPowsyblComputationMpiMpiTask::init(JNIEnv* env) {
    jclass _class = env->FindClass("com/powsybl/computation/mpi/MpiTask");
    _getIdMethodId = env->GetMethodID(_class, "getId", "()I");    
    _getRankMethodId = env->GetMethodID(_class, "getRank", "()I");
    _getThreadMethodId = env->GetMethodID(_class, "getThread", "()I");
    _getMessageMethodId = env->GetMethodID(_class, "getMessage", "()[B");
    _setResultMessageMethodId = env->GetMethodID(_class, "setResultMessage", "([B)V");    	
}

int ComPowsyblComputationMpiMpiTask::id() const {
    return _env->CallIntMethod(_obj, _getIdMethodId);
}

int ComPowsyblComputationMpiMpiTask::rank() const {
	return _env->CallIntMethod(_obj, _getRankMethodId);
}

int ComPowsyblComputationMpiMpiTask::thread() const {
    return _env->CallIntMethod(_obj, _getThreadMethodId);
}

jbyteArray ComPowsyblComputationMpiMpiTask::message() const {
	return (jbyteArray) _env->CallObjectMethod(_obj, _getMessageMethodId);
}

void ComPowsyblComputationMpiMpiTask::resultMessage(const std::string& buffer) {
	jbyteArray jresult = _env->NewByteArray(buffer.size());       
    _env->SetByteArrayRegion(jresult, 0, buffer.size(), (jbyte*) &buffer[0]);
	_env->CallVoidMethod(_obj, _setResultMessageMethodId, jresult);
}

}

}
