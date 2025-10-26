# 命令系统使用说明

## 命令结构

所有命令都以 `/ncef` 开头，需要权限等级 2（管理员）。

### 命令格式
```
/ncef <玩家名> <命令类型> [参数]
```

## 可用命令

### 1. openGui - 打开简单浏览器（无工具栏）
```
/ncef <玩家名> openGui
```

**功能**：
- 打开简单的 WebScreen
- 没有地址栏和导航按钮
- 只显示浏览器内容
- 适合嵌入式网页展示

**示例**：
```
/ncef Steve openGui
```

**对应方法**：
- `BrowserNetworkHandler.sendOpenGui(player)`
- 发送 `CommandType.OPEN_GUI` 命令
- 客户端打开 `new BrowserScreen()`

---

### 2. openBrowser - 打开完整浏览器（带工具栏）
```
/ncef <玩家名> openBrowser
```

**功能**：
- 打开完整的 BrowserScreen
- 包含地址栏、前进/后退/刷新按钮
- 支持输入 URL 和导航
- 类似真实浏览器体验

**示例**：
```
/ncef Steve openBrowser
```

**对应方法**：
- `BrowserNetworkHandler.sendOpenBrowser(player)`
- 发送 `CommandType.OPEN_BROWSER` 命令
- 客户端打开 `new BrowserScreen()`

---

### 3. loadUrl - 加载指定 URL
```
/ncef <玩家名> loadUrl <URL>
```

**功能**：
- 打开 BrowserScreen 并加载指定 URL
- 自动导航到该网址
- 支持 http/https/file 协议

**示例**：
```
/ncef Steve loadUrl https://www.google.com
/ncef Steve loadUrl https://github.com
/ncef Steve loadUrl file:///C:/test.html
```

**对应方法**：
- `BrowserNetworkHandler.sendLoadUrl(player, url)`
- 发送 `CommandType.LOAD_URL` 命令
- 客户端打开 `new BrowserScreen(url)`

---

## 命令执行流程

### 架构图
```
Server端:
  命令输入 → CommonCommand.buildCommandTree()
    ↓
  执行对应的 execute 方法
    ↓
  调用 BrowserNetworkHandler.sendXxx(player)
    ↓
  创建 BrowserPacket 并发送到客户端

Client端:
  接收数据包 → BrowserNetworkHandler.registerClientReceiver()
    ↓
  解码 BrowserPacket
    ↓
  调用 ClientEnumeration(commandType, body)
    ↓
  根据 commandType 打开对应的 Screen
```

### 代码执行路径

#### 1. openGui 命令
```java
// 服务端
/ncef Steve openGui
  → CommonCommand.executePlayerCommand(ctx, OPEN_GUI)
  → networkHandler.sendOpenGui(player)
  → sendToPlayer(player, OPEN_GUI, "")
  → 发送网络包

// 客户端
→ registerClientReceiver() 接收
→ new ClientEnumeration(OPEN_GUI, "")
→ case OPEN_GUI: new BrowserScreen()
```

#### 2. openBrowser 命令
```java
// 服务端
/ncef Steve openBrowser
  → CommonCommand.executePlayerCommand(ctx, OPEN_BROWSER)
  → networkHandler.sendOpenBrowser(player)
  → sendToPlayer(player, OPEN_BROWSER, "")
  → 发送网络包

// 客户端
→ registerClientReceiver() 接收
→ new ClientEnumeration(OPEN_BROWSER, "")
→ case OPEN_BROWSER: new BrowserScreen()
```

#### 3. loadUrl 命令
```java
// 服务端
/ncef Steve loadUrl https://google.com
  → CommonCommand.executePlayerCommandWithArg(ctx, LOAD_URL, url)
  → networkHandler.sendLoadUrl(player, url)
  → sendToPlayer(player, LOAD_URL, url)
  → 发送网络包

// 客户端
→ registerClientReceiver() 接收
→ new ClientEnumeration(LOAD_URL, "https://google.com")
→ case LOAD_URL: new BrowserScreen("https://google.com")
```

## 关键组件说明

### CommandType 枚举
```java
public enum CommandType {
    OPEN_GUI("openGui"),        // 简单浏览器
    LOAD_URL("loadUrl"),        // 加载 URL
    OPEN_BROWSER("openBrowser"); // 完整浏览器
    
    private final String commandName;
}
```

### BrowserNetworkHandler 方法
```java
public class BrowserNetworkHandler {
    // 打开简单 GUI（无工具栏）
    public void sendOpenGui(ServerPlayer player) { ... }
    
    // 打开完整浏览器（带工具栏）
    public void sendOpenBrowser(ServerPlayer player) { ... }
    
    // 加载指定 URL
    public void sendLoadUrl(ServerPlayer player, String url) { ... }
}
```

### ClientEnumeration 处理
```java
public ClientEnumeration(CommandType messageType, String body) {
    switch (messageType) {
        case OPEN_GUI -> new BrowserScreen();
        case LOAD_URL -> new BrowserScreen(body);
        case OPEN_BROWSER -> new BrowserScreen(body);
    }
}
```

## 区别对比

| 命令 | 工具栏 | 地址栏 | 导航按钮 | 初始URL | 用途 |
|------|--------|--------|----------|---------|------|
| openGui | ❌ | ❌ | ❌ | 空白页 | 简单展示 |
| openBrowser | ✅ | ✅ | ✅ | 空白页 | 完整浏览 |
| loadUrl | ✅ | ✅ | ✅ | 指定URL | 打开网址 |

## 扩展命令示例

### 添加新命令（示例：打开开发者工具）

1. **添加枚举**：
```java
// CommandType.java
public enum CommandType {
    OPEN_GUI("openGui"),
    LOAD_URL("loadUrl"),
    OPEN_BROWSER("openBrowser"),
    OPEN_DEVTOOLS("openDevTools"); // 新增
}
```

2. **添加网络处理器方法**：
```java
// BrowserNetworkHandler.java
public void sendOpenDevTools(ServerPlayer player) {
    sendToPlayer(player, CommandType.OPEN_DEVTOOLS, "");
}
```

3. **添加命令注册**：
```java
// CommonCommand.java
public LiteralArgumentBuilder<CommandSourceStack> buildCommandTree() {
    return Commands.literal("ncef")
        .then(playerCommand(CommandType.OPEN_GUI))
        .then(playerCommand(CommandType.OPEN_BROWSER))
        .then(playerCommand(CommandType.OPEN_DEVTOOLS)) // 新增
        .then(playerCommandWithArg(CommandType.LOAD_URL, "url"));
}

private int executePlayerCommand(...) {
    switch (commandType) {
        case OPEN_GUI -> networkHandler.sendOpenGui(targetPlayer);
        case OPEN_BROWSER -> networkHandler.sendOpenBrowser(targetPlayer);
        case OPEN_DEVTOOLS -> networkHandler.sendOpenDevTools(targetPlayer); // 新增
    }
}
```

4. **添加客户端处理**：
```java
// ClientEnumeration.java
switch (messageType) {
    case OPEN_GUI -> new BrowserScreen();
    case OPEN_BROWSER -> new BrowserScreen();
    case LOAD_URL -> new BrowserScreen(body);
    case OPEN_DEVTOOLS -> openDevTools(); // 新增
}
```

## 权限控制

当前所有命令需要权限等级 2：
```java
.requires(src -> src.hasPermission(2))
```

如需修改权限：
- 等级 0：所有玩家
- 等级 1：普通玩家
- 等级 2：管理员（默认）
- 等级 3：高级管理员
- 等级 4：服务器所有者

## 测试建议

1. **单人模式测试**：
   ```
   /ncef @s openGui
   /ncef @s openBrowser
   /ncef @s loadUrl https://www.minecraft.net
   ```

2. **多人模式测试**：
   ```
   /ncef PlayerName openBrowser
   ```

3. **选择器测试**：
   ```
   /ncef @a openGui              # 所有玩家
   /ncef @p openBrowser          # 最近的玩家
   /ncef @r loadUrl https://...  # 随机玩家
   ```

