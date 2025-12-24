package top.yunmouren.craftbrowser.server.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.client.network.ClientEnumeration;
import top.yunmouren.craftbrowser.server.command.CommandType;
import top.yunmouren.httpserver.HttpNetworkHandler;

public final class BrowserNetworkHandler {
    public static void registerS2CPayLoad(){
        NetworkManager.registerS2CPayloadType(
                BrowserPayload.TYPE,
                BrowserPayload.CODEC
        );
    }
    public static void registerS2C() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                BrowserPayload.TYPE,
                BrowserPayload.CODEC,
                (payload, context) -> {
                    context.queue(() -> {
                        new ClientEnumeration(
                                payload.command(),
                                payload.body()
                        );
                    });
                }
        );
    }

    public static void sendOpenGui(ServerPlayer player) {
        sendToPlayer(player, CommandType.OPEN_GUI, "");
    }

    public static void sendLoadUrl(ServerPlayer player, String url) {
        sendToPlayer(player, CommandType.LOAD_URL, url);
    }

    private static void sendToPlayer(ServerPlayer player, CommandType type, String body) {
        NetworkManager.sendToPlayer(
                player,
                new BrowserPayload(type, body == null ? "" : body)
        );
    }
}
