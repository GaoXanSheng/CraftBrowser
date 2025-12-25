package top.yunmouren.craftbrowser.server.network.annotation;

import dev.architectury.networking.NetworkManager;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketConfig {
    NetworkManager.Side side();
    boolean async() default false;
}