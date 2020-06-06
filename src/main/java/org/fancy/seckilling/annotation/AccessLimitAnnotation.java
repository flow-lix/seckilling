package org.fancy.seckilling.annotation;

import java.lang.annotation.*;

/**
 * @author lix
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimitAnnotation {

    /**
     * 调用最大次数
     */
    int maxLimit();

    int timeoutSeconds();

    boolean needLogin() default false;
}
