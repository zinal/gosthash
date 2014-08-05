// $Id: GostHashNative.java 45536 2013-04-08 13:23:58Z zinal $
package ru.zinal.gosthash;

import java.io.File;

/**
 * Реализация расчета хеш-суммы ГОСТ 34.11-94 на C.
 * @author zinal
 * @since 2011-01-13
 */
public class GostHashNative implements GostHashIface {

  private native static long doInit();
  private native static void doDone(long ctx);
  private native static void doStartHash(long ctx);
  private native static void doHashBlock(long ctx, byte[] block, int pos, int length);
  private native static byte[] doFinishHash(long ctx);
  private native static byte[] doCalcHash(long ctx, String fname);

  private long ctx = 0L;

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if ( ctx!=0L )
      doDone(ctx);
  }

  public boolean isNative() {
    return true;
  }

  public void init() {
    if ( ctx!=0L ) {
      doDone(ctx);
      ctx = 0L;
    }
    ctx = doInit();
  }

  public void done() {
    if ( ctx!=0L ) {
      doDone(ctx);
      ctx = 0L;
    }
  }

  public void startHash() {
    doStartHash(ctx);
  }

  public void hashBlock(byte[] block, int pos, int length) {
    if ( length < 0 && block != null )
      length = block.length;
    if ( block==null || pos < 0 || length <= 0 || pos+length > block.length )
      throw new RuntimeException("Некорректные аргументы вызова метода");
    doHashBlock(ctx, block, pos, length);
  }

  public byte[] finishHash() {
    return doFinishHash(ctx);
  }

  public byte[] calcHash(File file) {
    if ( ! file.exists() || ! file.isFile() )
      return null;
    return doCalcHash(ctx, file.getAbsolutePath());
  }

}
