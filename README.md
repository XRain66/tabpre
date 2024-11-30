# TabPre - Velocity Tab列表前缀插件

[![Build Status](https://github.com/[你的用户名]/[仓库名]/workflows/Build/badge.svg)](https://github.com/[你的用户名]/[仓库名]/actions)

一个用于自定义玩家Tab列表前缀的Velocity插件。

## 功能特点

- 为不同玩家设置自定义前缀
- 支持颜色代码
- 实时更新Tab列表
- 简单的命令系统
- 完整的权限控制

## 安装

1. 下载最新版本的插件
2. 将插件放入Velocity服务器的`plugins`文件夹
3. 启动或重启服务器

## 配置

配置文件位于 `plugins/tabpre/config.yml`：

```yaml
# 玩家前缀配置
prefixes:
  玩家名: "&c[VIP]&r "  # 将显示为红色的[VIP]前缀
  其他玩家: "&6[MVP]&r " # 将显示为金色的[MVP]前缀
```

## 命令

- `/tabprefix help` - 显示帮助信息
- `/tabprefix reload` - 重新加载配置（需要权限）

## 权限

- `tabpre.reload` - 允许重新加载配置

## 颜色代码

支持以下颜色代码：
- &0 黑色
- &1 深蓝色
- &2 深绿色
- &3 湖蓝色
- &4 深红色
- &5 紫色
- &6 金色
- &7 灰色
- &8 深灰色
- &9 蓝色
- &a 绿色
- &b 天蓝色
- &c 红色
- &d 粉红色
- &e 黄色
- &f 白色
- &r 重置格式

## 开发

### 构建
```bash
./gradlew build
```

### 测试
```bash
./gradlew test
```

### 代码质量
```bash
./gradlew checkstyleMain
```

## 支持

如果你遇到任何问题，请在GitHub上创建一个issue。 