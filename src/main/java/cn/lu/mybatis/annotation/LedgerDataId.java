package cn.lu.mybatis.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface LedgerDataId {
}