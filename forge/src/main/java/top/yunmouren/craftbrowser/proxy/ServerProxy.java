package top.yunmouren.craftbrowser.proxy;

import top.yunmouren.craftbrowser.server.Server;


public class ServerProxy implements IProxy {
    @Override
    public void init() {
        new Server();
    }
}