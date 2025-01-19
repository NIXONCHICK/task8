package task8.services;

import task8.annotations.Cache;
import task8.annotations.CacheType;

public interface Service {

  @Cache(cacheType = CacheType.FILE, fileNamePrefix = "hardWork", zip = true, identityBy = {String.class})
  double doHardWork(String work, int value);

  @Cache(cacheType = CacheType.FILE, fileNamePrefix = "cachedArray", zip = true)
  String[] processArray(String prefix, int size);
}
