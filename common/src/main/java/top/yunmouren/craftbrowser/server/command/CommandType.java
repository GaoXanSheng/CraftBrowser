package top.yunmouren.craftbrowser.server.command;


public enum CommandType {
    OPEN_GUI("openGui"),
    LOAD_URL("loadUrl");

    private final String commandName;

    CommandType(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}

