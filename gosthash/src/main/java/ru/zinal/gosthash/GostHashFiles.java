// $Id: GostHashFiles.java 34991 2011-08-23 11:11:01Z zinal $
package ru.zinal.gosthash;

/**
 * Подсчет контрольных сумм файлов, заданных как аргументы
 * @author zinal
 * @since 2011-01-13
 */
public class GostHashFiles {

  public static void main(String[] args) {
    for ( int i=0; i<args.length; ++i )
      System.out.println(args[i]+": "+GostHash.calcStr(args[i]));
  }

}
