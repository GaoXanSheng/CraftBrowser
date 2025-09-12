package top.yunmouren.craftbrowser.server.network;

import net.minecraftforge.network.NetworkDirection;

/**
 * 网络消息类型
 */
public enum NetworkMessageType {

    OPEN_DEV("OpenDevTools", NetworkDirection.PLAY_TO_CLIENT),
    LOAD_URL("LoadUrl", NetworkDirection.PLAY_TO_CLIENT);

    private final String id;
    private final NetworkDirection direction;

    NetworkMessageType(String id, NetworkDirection direction) {
        this.id = id;
        this.direction = direction;
    }

    public String getId() {
        return id;
    }

    public NetworkDirection getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return id;
    }

    public static NetworkMessageType fromId(String id) {
        for (NetworkMessageType type : values()) {
            if (type.id.equals(id)) return type;
        }
        throw new IllegalArgumentException("Unknown NetworkMessageType: " + id);
    }
}
