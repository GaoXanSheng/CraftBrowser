package top.yunmouren.craftbrowser.server.network;

public enum NetworkMessageType {

    OPEN_DEV("OPEN_DEV", Direction.PLAY_TO_CLIENT),
    LOAD_URL("LOAD_URL", Direction.PLAY_TO_CLIENT);

    private final String id;
    private final Direction direction;

    NetworkMessageType(String id, Direction direction) {
        this.id = id;
        this.direction = direction;
    }

    public String getId() {
        return id;
    }

    public Direction getDirection() {
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

    /**
     * 自定义方向枚举，替代 Forge 的 NetworkDirection
     */
    public enum Direction {
        PLAY_TO_CLIENT,
        PLAY_TO_SERVER
    }
}
