package cn.lu.mybatis.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface LedgerData {

    /**
     * 对应的数据库表名
     *
     * @return
     */
    String tableName() default "";

    /**
     * 哪个字段作为主键,用于做链上的data_id
     *
     * @return
     */
    String keyFieldName() default "";
}