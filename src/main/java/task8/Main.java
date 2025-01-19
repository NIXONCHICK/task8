package task8;

import task8.proxy.CacheProxy;
import task8.services.Service;
import task8.services.ServiceImpl;

import java.io.File;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    CacheProxy cacheProxy = new CacheProxy(new File("cache"));
    Service service = cacheProxy.cache(new ServiceImpl());

    // Тест кеширования массива
    String[] array1 = service.processArray("item", 5); // Создаётся массив
    System.out.println("Массив 1: " + Arrays.toString(array1));

    String[] array2 = service.processArray("item", 5); // Берётся из кеша
    System.out.println("Массив 2: " + Arrays.toString(array2));
  }
}
