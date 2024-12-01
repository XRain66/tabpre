# TabPre

一个用于自定义玩家 Tab 列表前缀的 Velocity 插件，解决了 Minecraft 1.17+ 中游戏模式同步的问题。通过 Fabric mod 支持，实现了准确的游戏模式显示和前缀管理。

## 功能特点

- 自定义玩家 Tab 列表前缀
- 实时同步玩家游戏模式
- 支持颜色代码
- 权限系统支持
- 配置文件热重载
- 跨服务器游戏模式同步

## 安装说明

1. 下载最新版本的插件
2. 将 `tabpre-velocity.jar` 放入 Velocity 的 `plugins` 文件夹
3. 将 `tabpre-fabric.jar` 放入 Fabric 服务端的 `mods` 文件夹
4. 启动服务器，插件会自动生成配置文件

## 系统要求

- Velocity 3.1.1+
- Fabric 服务端 (Minecraft 1.17.1)
- Java 16 或更高版本

## 配置文件

配置文件位于 `plugins/tabpre/config.yml`：

```yaml
# 玩家前缀配置
prefixes:
  玩家名: "&c[前缀] &f"

# 消息配置
messages:
  reload-success: "&a配置重载成功！"
  no-permission: "&c你没有权限执行此命令！"
  # ...
```

## 命令

- `/tabprefix reload` - 重新加载配置
- `/tabprefix debug <玩家>` - 显示玩家的 TabList 信息
- `/tabprefix help` - 显示帮助信息

## 权限

- `tabpre.reload` - 允许重载配置
- `tabpre.debug` - 允许使用调试命令

## 注意事项

1. 颜色代码使用 `&` 符号
2. 前缀支持所有 Minecraft 颜色代码
3. 配置文件修改后需要使用 reload 命令重新加载
4. 确保所有子服都安装了对应版本的 Fabric mod

## 常见问题

Q: 为什么前缀没有显示？
A: 请检查：
1. 配置文件格式是否正确
2. 玩家名是否完全匹配
3. 是否正确使用了颜色代码

Q: 游戏模式不同步怎么办？
A: 确保：
1. Fabric 端已正确安装 mod
2. 服务器与 Velocity 的通信正常
3. 没有其他插件干扰
4. 使用了正确的 Minecraft 版本 (1.17.1)

## 技术支持

如果遇到问题，请：
1. 查看控制台日志
2. 使用 debug 命令检查玩家状态
3. 在 GitHub 上提交 issue

## 构建说明

1. 克隆仓库：
```bash
git clone https://github.com/XRain66/tabpre.git
```

2. 构建项目：
```bash
./gradlew build
```

构建完成后，你可以在以下位置找到生成的文件：
- Velocity 插件: `velocity/build/libs/tabpre-velocity-*.jar`
- Fabric mod: `fabric/build/libs/tabpre-fabric-*.jar`

## 开源协议

MIT License 