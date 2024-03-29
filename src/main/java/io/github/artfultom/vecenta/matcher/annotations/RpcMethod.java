package io.github.artfultom.vecenta.matcher.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcMethod {

    String entity();

    String name();

    String[] argumentTypes() default "";

    String returnType() default "";

    String[] errors() default "";
}
