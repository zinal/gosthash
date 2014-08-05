/* $Id: gostjava.c 30932 2011-01-14 08:04:26Z zinal $ */

#include <memory.h>
#include <jni.h>
#include "gosthash.h"

/*
 * Class:     ru_zinal_gosthash_GostHashNative
 * Method:    doInit
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_ru_zinal_gosthash_GostHashNative_doInit
  (JNIEnv *env, jclass jc)
{
  gost_hash_ctx* ctx;
  ctx = malloc(sizeof(gost_hash_ctx));
  if ( ctx==NULL )
    return 0L;
  init_gost_hash_ctx(ctx, &GostR3411_94_CryptoProParamSet);
  return (jlong)(size_t)ctx;
}

/*
 * Class:     ru_zinal_gosthash_GostHashNative
 * Method:    doDone
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_ru_zinal_gosthash_GostHashNative_doDone
  (JNIEnv *env, jclass jc, jlong jctx)
{
  gost_hash_ctx* ctx = (gost_hash_ctx*)(size_t)jctx;
  if ( ctx==NULL )
    return;
  done_gost_hash_ctx(ctx);
  free(ctx);
}

/*
 * Class:     ru_zinal_gosthash_GostHashNative
 * Method:    doStartHash
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_ru_zinal_gosthash_GostHashNative_doStartHash
  (JNIEnv *env, jclass jc, jlong jctx)
{
  gost_hash_ctx* ctx = (gost_hash_ctx*)(size_t)jctx;
  if ( ctx==NULL )
    return;
  start_hash(ctx);
}

/*
 * Class:     ru_zinal_gosthash_GostHashNative
 * Method:    doHashBlock
 * Signature: (J[BII)V
 */
JNIEXPORT void JNICALL Java_ru_zinal_gosthash_GostHashNative_doHashBlock
  (JNIEnv *env, jclass jc, jlong jctx, jbyteArray buf, jint pos, jint len)
{
  jbyte* ptr;
  gost_hash_ctx* ctx = (gost_hash_ctx*)(size_t)jctx;
  if ( ctx==NULL )
    return;
  ptr = (*env)->GetByteArrayElements(env, buf, NULL);
  hash_block(ctx, (unsigned char*)ptr, len);
  (*env)->ReleaseByteArrayElements(env, buf, ptr, 0);
}

/*
 * Class:     ru_zinal_gosthash_GostHashNative
 * Method:    doFinishHash
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_ru_zinal_gosthash_GostHashNative_doFinishHash
  (JNIEnv * env, jclass jc, jlong jctx)
{
  unsigned char buf[32];
  jbyteArray retval;
  gost_hash_ctx* ctx = (gost_hash_ctx*)(size_t)jctx;
  if ( ctx==NULL )
    return NULL;
  finish_hash(ctx, buf);
  retval = (*env)->NewByteArray(env, 32);
  if ( retval != NULL ) {
    jbyte* ptr = (*env)->GetByteArrayElements(env, retval, NULL);
    if ( ptr!=NULL ) {
      memcpy(ptr, buf, 32);
      (*env)->ReleaseByteArrayElements(env, retval, ptr, 0);
    }
  }
  return retval;
}

/*
 * Class:     ru_zinal_gosthash_GostHashNative
 * Method:    doCalcHash
 * Signature: (JLjava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_ru_zinal_gosthash_GostHashNative_doCalcHash
  (JNIEnv *env, jclass jc, jlong jctx, jstring jfname)
{
  unsigned char buf[256];
  jbyteArray retval;
  const char* fname;
  FILE* f;
  int haveError;

  gost_hash_ctx* ctx = (gost_hash_ctx*)(size_t)jctx;
  if ( ctx==NULL )
    return NULL;

  fname = (*env)->GetStringUTFChars(env, jfname, NULL);
  if ( fname==NULL )
    return NULL;

  f = fopen(fname, "rb");
  (*env)->ReleaseStringUTFChars(env, jfname, fname);
  if ( f==NULL )
    return NULL;
  
  haveError = 0;
  start_hash(ctx);
  while (1) {
    size_t len = fread(buf, 1, sizeof(buf), f);
    if ( len==0 ) {
      if ( ferror(f) )
        haveError = 1;
      break;
    }
    hash_block(ctx, buf, len);
  }
  finish_hash(ctx, buf);
  fclose(f);
  
  if ( haveError )
    return NULL;
  
  retval = (*env)->NewByteArray(env, 32);
  if ( retval != NULL ) {
    jbyte* ptr = (*env)->GetByteArrayElements(env, retval, NULL);
    if ( ptr!=NULL ) {
      memcpy(ptr, buf, 32);
      (*env)->ReleaseByteArrayElements(env, retval, ptr, 0);
    }
  }
  return retval;
}

/* End Of File */
