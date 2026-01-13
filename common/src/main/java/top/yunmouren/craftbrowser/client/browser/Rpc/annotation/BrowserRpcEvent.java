package top.yunmouren.craftbrowser.client.browser.Rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BrowserRpcEvent {

    String value() default "";

    boolean runOnMainThread() default true;
}