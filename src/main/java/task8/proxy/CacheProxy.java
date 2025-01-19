package task8.proxy;

import task8.annotations.Cache;
import task8.annotations.CacheType;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CacheProxy {
  private final File cacheDir;
  private final Map<String, Object> inMemoryCache = new HashMap<>();

  public CacheProxy(File cacheDir) {
    this.cacheDir = cacheDir;
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T cache(T service) {
    return (T) Proxy.newProxyInstance(
        service.getClass().getClassLoader(),
        service.getClass().getInterfaces(),
        (proxy, method, args) -> {
          Cache cache = method.getAnnotation(Cache.class);
          if (cache == null) {
            return method.invoke(service, args);
          }

          String cacheKey = generateCacheKey(method, args, cache);

          if (cache.cacheType() == CacheType.IN_MEMORY) {
            if (inMemoryCache.containsKey(cacheKey)) {
              System.out.println("Возвращаем результат из памяти для " + cacheKey);
              return inMemoryCache.get(cacheKey);
            }
            Object result = method.invoke(service, args);
            result = limitListSizeIfNeeded(result, cache);
            inMemoryCache.put(cacheKey, result);
            return result;
          }

          // Работа с файловым кешом
          File cacheFile = new File(cacheDir, cacheKey + (cache.zip() ? ".zip" : ".cache"));
          if (cacheFile.exists()) {
            System.out.println("Возвращаем результат из файла для " + cacheKey);
            try (ObjectInputStream ois = cache.zip() ?
                new ObjectInputStream(getInputStreamFromZip(cacheFile, cacheKey)) :
                new ObjectInputStream(new FileInputStream(cacheFile))) {
              return ois.readObject();
            }
          }

          Object result = method.invoke(service, args);
          result = limitListSizeIfNeeded(result, cache);

          try (ObjectOutputStream oos = cache.zip() ?
              new ObjectOutputStream(getOutputStreamToZip(cacheFile, cacheKey)) :
              new ObjectOutputStream(new FileOutputStream(cacheFile))) {
            oos.writeObject(result);
          } catch (NotSerializableException e) {
            throw new IllegalArgumentException(
                "Результат метода " + method.getName() + " не сериализуем. Убедитесь, что возвращаемый объект реализует Serializable.",
                e
            );
          }
          return result;
        }
    );
  }

  private String generateCacheKey(Method method, Object[] args, Cache cache) {
    StringBuilder keyBuilder = new StringBuilder();
    keyBuilder.append(cache.fileNamePrefix().isEmpty() ? method.getName() : cache.fileNamePrefix());
    for (Object arg : args) {
      keyBuilder.append("_").append(arg);
    }
    return keyBuilder.toString();
  }

  private InputStream getInputStreamFromZip(File zipFile, String entryName) throws IOException {
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
      if (entry.getName().equals(entryName)) {
        return zis;
      }
    }
    throw new FileNotFoundException("Entry " + entryName + " not found in ZIP archive.");
  }

  private OutputStream getOutputStreamToZip(File zipFile, String entryName) throws IOException {
    FileOutputStream fos = new FileOutputStream(zipFile);
    ZipOutputStream zos = new ZipOutputStream(fos);
    zos.putNextEntry(new ZipEntry(entryName));
    return zos;
  }

  private Object limitListSizeIfNeeded(Object result, Cache cache) {
    if (result instanceof List && cache.listLimit() < ((List<?>) result).size()) {
      System.out.println("Ограничиваем размер списка до " + cache.listLimit());
      return ((List<?>) result).subList(0, cache.listLimit());
    }
    return result;
  }
}
