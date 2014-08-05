// $Id: GostHashIface.java 34991 2011-08-23 11:11:01Z zinal $
package ru.zinal.gosthash;

/**
 * Интерфейс реализации алгоритма подсчета хеш-суммы ГОСТ Р 34.11-94
 * @author zinal
 * @since 2011-01-13
 */
public interface GostHashIface {

  /**
   * Вернуть признак типа реализации
   * @return true, если реализовано через JNI-библиотеку.
   */
  boolean isNative();

  /**
   * Первоначальная инициализация
   */
  void init();

  /**
   * Итоговая очистка
   */
  void done();

  /**
   * Подготовка к расчету хеш-суммы
   */
  void startHash();

  /**
   * Добавить в хеш очередной блок данных
   * @param block
   * @param pos
   * @param length
   */
  void hashBlock(byte[] block, int pos, int length);

  /**
   * Получить результат - хеш-сумму
   * @return Подсчитанное значение хэша
   */
  byte[] finishHash();

  /**
   * Подсчитать контрольную сумму файла
   * При ошибке ввода-вывода (включая отсутствие файла) вернуть null.
   * @param file
   * @return Подсчитанное значение хэша
   */
  byte[] calcHash(java.io.File file);

}
