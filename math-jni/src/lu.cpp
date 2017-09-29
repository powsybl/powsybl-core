// Copyright (c) 2017, RTE (http://www.rte-france.com)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file lu.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <string>
#include <iostream>
#include <map>
#include <memory>
#include <mutex>
#include <suitesparse/klu.h>
#include <suitesparse/cs.h>
#include "jniwrapper.hpp"

namespace powsybl {

namespace jni {

class ComPowsyblMathMatrixSparseMatrix : public JniWrapper<jobject> {
public:
    ComPowsyblMathMatrixSparseMatrix(JNIEnv* env, int m, int n, const IntArray& ap, const IntArray& ai, const DoubleArray& ax);

    static void init(JNIEnv* env);

private:
    static jclass _cls;
    static jmethodID _constructor; 
};

jclass ComPowsyblMathMatrixSparseMatrix::_cls = 0;
jmethodID ComPowsyblMathMatrixSparseMatrix::_constructor = 0;

void ComPowsyblMathMatrixSparseMatrix::init(JNIEnv* env) {
    jclass localCls = env->FindClass("com/powsybl/math/matrix/SparseMatrix");
    _cls = reinterpret_cast<jclass>(env->NewGlobalRef(localCls));
    _constructor = env->GetMethodID(_cls, "<init>", "(II[I[I[D)V");
}

ComPowsyblMathMatrixSparseMatrix::ComPowsyblMathMatrixSparseMatrix(JNIEnv* env, int m, int n, const IntArray& ap, const IntArray& ai, const DoubleArray& ax)
    : JniWrapper<jobject>(env, env->NewObject(_cls, _constructor, m, n, ap.obj(), ai.obj(), ax.obj())) {
}

}

}

struct LUContext {
    LUContext() {
    }

    klu_symbolic* symbolic;
    klu_numeric* numeric;
    klu_common common;

    std::string error() const;

private:
    LUContext(const LUContext&);
    LUContext& operator=(const LUContext&);
};

std::string LUContext::error() const {
    switch (common.status) {
        case KLU_OK: return "KLU_OK";
        case KLU_SINGULAR: return "KLU_SINGULAR";
        case KLU_OUT_OF_MEMORY: return "KLU_OUT_OF_MEMORY";
        case KLU_INVALID: return "KLU_INVALID";
        case KLU_TOO_LARGE: return "KLU_TOO_LARGE";
        default: throw std::runtime_error("Unknown KLU status");
    }
}

class LUContextManager {

    LUContextManager(const LUContextManager&);
    LUContextManager& operator=(const LUContextManager&);

    std::map<std::string, std::shared_ptr<LUContext>> _contexts;
    std::mutex _mutex;

public:
    LUContextManager() {
    }

    std::shared_ptr<LUContext> createContext(const std::string& id);
    std::shared_ptr<LUContext> findContext(const std::string& id);
    void removeContext(const std::string& id);
};

std::shared_ptr<LUContext> LUContextManager::createContext(const std::string& id) {
    std::lock_guard<std::mutex> lk(_mutex);
    if (_contexts.find(id) != _contexts.end()) {
        throw std::runtime_error("Context " + id + " already exists");
    }
    std::shared_ptr<LUContext> context(new LUContext());
    _contexts[id] = context;
    return context;
}

std::shared_ptr<LUContext> LUContextManager::findContext(const std::string& id) {
    std::lock_guard<std::mutex> lk(_mutex);
    std::map<std::string, std::shared_ptr<LUContext>>::const_iterator it = _contexts.find(id);
    if (it == _contexts.end()) {
        throw std::runtime_error("Context " + id + " not found");
    }
    return it->second;
}

void LUContextManager::removeContext(const std::string& id) {
    std::lock_guard<std::mutex> lk(_mutex);
    _contexts.erase(id);
}

std::unique_ptr<LUContextManager> MANAGER(new LUContextManager());

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com.powsybl_math_matrix_SparseLUDecomposition
 * Method:    init
 * Signature: (Ljava/lang/String;[I[I[D)V
 */
JNIEXPORT void JNICALL Java_com_powsybl_math_matrix_SparseLUDecomposition_init(JNIEnv * env, jobject, jstring j_id, jintArray j_ap, jintArray j_ai, jdoubleArray j_ax) {
    try {
        std::string id = powsybl::jni::StringUTF(env, j_id).toStr();
        powsybl::jni::IntArray ap(env, j_ap);
        powsybl::jni::IntArray ai(env, j_ai);
        powsybl::jni::DoubleArray ax(env, j_ax);

        std::shared_ptr<LUContext> context = MANAGER->createContext(id);
 
        if (klu_defaults(&context->common) == 0) {
            throw std::runtime_error("klu_defaults error " + context->error());
        }

        context->symbolic = klu_analyze(ap.length()-1, ap.get(), ai.get(), &context->common);
        if (!context->symbolic) {
            throw std::runtime_error("klu_analyze error " + context->error());
        }
        context->numeric = klu_factor(ap.get(), ai.get(), ax.get(), context->symbolic, &context->common);
        if (!context->numeric) {
            throw std::runtime_error("klu_factor error " + context->error());
        }
    } catch (const std::exception& e) {
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_math_matrix_SparseLUDecomposition
 * Method:    release
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_powsybl_math_matrix_SparseLUDecomposition_release(JNIEnv * env, jobject, jstring j_id) {
    try {
        std::string id = powsybl::jni::StringUTF(env, j_id).toStr();

        std::shared_ptr<LUContext> context = MANAGER->findContext(id);

        if (klu_free_symbolic(&context->symbolic, &context->common) == 0) {
            throw std::runtime_error("klu_free_symbolic error " + context->error());
        }
        if (klu_free_numeric(&context->numeric, &context->common) == 0) {
            throw std::runtime_error("klu_free_numeric error " + context->error());
        }

        MANAGER->removeContext(id);
    } catch (const std::exception& e) {
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_math_matrix_SparseLUDecomposition
 * Method:    solve
 * Signature: (Ljava/lang/String;[D)V
 */
JNIEXPORT void JNICALL Java_com_powsybl_math_matrix_SparseLUDecomposition_solve(JNIEnv * env, jobject, jstring j_id, jdoubleArray j_b) {
    try {
        std::string id = powsybl::jni::StringUTF(env, j_id).toStr();
        powsybl::jni::DoubleArray b(env, j_b);

        std::shared_ptr<LUContext> context = MANAGER->findContext(id);

        if (klu_solve(context->symbolic, context->numeric, b.length(), 1, b.get(), &context->common) == 0) {
            throw std::runtime_error("klu_solve error " + context->error());
        }
    } catch (const std::exception& e) {
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_math_matrix_SparseLUDecomposition
 * Method:    solve2
 * Signature: (Ljava/lang/String;IILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_com_powsybl_math_matrix_SparseLUDecomposition_solve2(JNIEnv * env, jobject, jstring j_id, jint m, jint n, jobject j_b) {
    try {
        std::string id = powsybl::jni::StringUTF(env, j_id).toStr();
        double* b = static_cast<double*>(env->GetDirectBufferAddress(j_b));
        if (!b) {
           throw std::runtime_error("GetDirectBufferAddress error");
        }

        std::shared_ptr<LUContext> context = MANAGER->findContext(id);

        if (klu_solve(context->symbolic, context->numeric, m, n, b, &context->common) == 0) {
            throw std::runtime_error("klu_solve error " + context->error());
        }
    } catch (const std::exception& e) {
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_math_matrix_SparseMatrix
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_powsybl_math_matrix_SparseMatrix_nativeInit(JNIEnv * env, jclass) {
    try {
        // lookup caching
        powsybl::jni::ComPowsyblMathMatrixSparseMatrix::init(env);
    } catch (const std::exception& e) {
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_math_matrix_SparseMatrix
 * Method:    times
 * Signature: (II[I[I[DII[I[I[D)Lcom/powsybl/math/matrix/SparseMatrix;
 */
JNIEXPORT jobject JNICALL Java_com_powsybl_math_matrix_SparseMatrix_times(JNIEnv * env, jobject, jint m1, jint n1, jintArray j_ap1, jintArray j_ai1, jdoubleArray j_ax1, 
                                                                                 jint m2, jint n2, jintArray j_ap2, jintArray j_ai2, jdoubleArray j_ax2) {
    try {
        powsybl::jni::IntArray ap1(env, j_ap1);
        powsybl::jni::IntArray ai1(env, j_ai1);
        powsybl::jni::DoubleArray ax1(env, j_ax1);
        powsybl::jni::IntArray ap2(env, j_ap2);
        powsybl::jni::IntArray ai2(env, j_ai2);
        powsybl::jni::DoubleArray ax2(env, j_ax2);

        cs_di a1;
        a1.nz = -1;
        a1.nzmax = ax1.length();
        a1.m = m1;
        a1.n = n1;
        a1.p = ap1.get();
        a1.i = ai1.get();
        a1.x = ax1.get();

        cs_di a2;
        a2.nz = -1;
        a2.nzmax = ax2.length();
        a2.m = m2;
        a2.n = n2;
        a2.p = ap2.get();
        a2.i = ai2.get();
        a2.x = ax2.get();

        cs_di* a3 = cs_di_multiply(&a1, &a2);

/*
        cs_di_print(&a1, 0);
        cs_di_print(&a2, 0);
        cs_di_print(a3, 0);
*/
    
        powsybl::jni::IntArray ap3(env, a3->p, a3->n + 1);
        powsybl::jni::IntArray ai3(env, a3->i, a3->nzmax);
        powsybl::jni::DoubleArray ax3(env, a3->x, a3->nzmax);
        return powsybl::jni::ComPowsyblMathMatrixSparseMatrix(env, a3->m, a3->n, ap3, ai3, ax3).obj();
    } catch (const std::exception& e) {
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
    return 0;
}

#ifdef __cplusplus
}
#endif
