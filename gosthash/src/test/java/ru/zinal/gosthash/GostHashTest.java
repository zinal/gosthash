// $Id$
package ru.zinal.gosthash;

import java.util.Random;
import org.junit.Test;
import org.junit.Before;

/**
 * Тест для хеша.
 * Хешируем по 10 4-х мегабайтных блоков в каждом контексте, не менее 1 минуты.
 * Меряем скорость хеширования в мегабайтах в секунду.
 * @author zinal
 * @since 2011-01-13
 */
public class GostHashTest {
  
  public GostHashTest() {
  }

  private int counter;
  private long startTime;
  private Random generator;
  private final byte[] data = new byte[4*1024*1024];
  
  @Before
  public void init() {
    counter = 0;
    startTime = 0L;
    generator = new Random();
  }
  
  @Test
  public void test() {
    fillRandomBlock();
    do {
      final GostHashIface hash = GostHash.createImpl();
      System.out.println("Тип реализации: " + (hash.isNative() ? "JNI" : "Java"));
    } while(false);

    startTime = System.currentTimeMillis();
    while ( true ) {
      if ( ! step1() )
        break;
    }

    final long df = (System.currentTimeMillis() - startTime);
    final int megs = counter * data.length / (1024*1024);
    double speed = ((double)megs) * 1000.0 / ((double)df);
    System.out.println("Обработано блоков: "+counter+", общая скорость: "+speed + " Мбайт/сек");
  }
  
  private void fillRandomBlock() {
    for ( int i=0; i<data.length; ++i ) {
      data[i] = (byte) (128 - generator.nextInt(255));
    }
  }

  private void modifyRandomBlock() {
    for ( int i=0; i<10; ++i ) {
      final int pos = generator.nextInt(data.length);
      data[pos] = (byte) (128 - generator.nextInt(255));
    }
  }

  private boolean step1() {
    final GostHashIface hash = GostHash.createImpl();
    hash.init();
    try {
      for ( int i=0; i<10; ++i )
        if ( ! step2(hash) )
          return false;
    } finally {
      hash.done();
    }
    return true;
  }

  private boolean step2(GostHashIface hash) {
    modifyRandomBlock();
    hash.startHash();
//    for ( int i=0; i<data.length / 1024; ++i )
//      hash.hashBlock(data, i*1024, 1024);
    hash.hashBlock(data, 0, data.length);
    hash.finishHash();
    ++counter;
    final long df = System.currentTimeMillis() - startTime;
    return (df < 60000L);
  }

}
