package cn.lu.mybatis.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LedgerDataField {

    /**
     * 对应链上的属性名
     *
     * @return
     */
    String fieldName() default "";
}