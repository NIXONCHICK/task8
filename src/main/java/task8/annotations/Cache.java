package task8.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
  CacheType cacheType() default CacheType.IN_MEMORY;

  int listLimit() default Integer.MAX_VALUE;

  Class<?>[] identityBy() default {};

  String fileNamePrefix() default "";

  boolean zip() default false;
}
