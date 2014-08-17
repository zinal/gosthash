// $Id$
package ru.zinal.gosthash;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class CheTest {
  
  public CheTest() {
  }
  
  @Test
  public void cheTest() throws Exception {
    GostHash.createImpl();
    final byte[] bytes = "Ч".getBytes("UTF-8");
    final byte[] javaResult;
    GostHashJava javaHash = new GostHashJava();
    try {
      javaHash.init();
      javaHash.startHash();
      javaHash.hashBlock(bytes, 0, bytes.length);
      javaResult = javaHash.finishHash();
    } finally {
      javaHash.done();
      javaHash = null;
    }
    final byte[] nativeResult;
    GostHashNative nativeHash = new GostHashNative();
    try {
      nativeHash.init();
      nativeHash.startHash();
      nativeHash.hashBlock(bytes, 0, bytes.length);
      nativeResult = nativeHash.finishHash();
    } finally {
      nativeHash.done();
      nativeHash = null;
    }
    System.out.println("JAVA:    " + GostHash.convert(javaResult));
    System.out.println("NATIVE:  " + GostHash.convert(nativeResult));
    Assert.assertArrayEquals(javaResult, nativeResult);
  }
  
}
