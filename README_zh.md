# FanTPA

一个功能强大的 Minecraft Fabric 传送模组，提供 TPA、Home、Back 等传送功能。

## 功能特性

### TPA 传送系统
| 指令 | 描述 | 权限 |
|------|------|------|
| `/tpa <玩家名>` | 向指定玩家发送传送请求 | 所有玩家 |
| `/tpahere <玩家名>` | 请求指定玩家传送到你的位置 | 所有玩家 |
| `/tpaccept <玩家名>` | 接受传送请求 | 所有玩家 |
| `/tpdeny <玩家名>` | 拒绝传送请求 | 所有玩家 |

### 家系统
| 指令 | 描述 | 权限 |
|------|------|------|
| `/home [家名]` | 传送到指定的家（不传家名则传送到默认家） | 所有玩家 |
| `/sethome [家名]` | 设置当前位置为家（不传家名则设为默认家） | 所有玩家 |
| `/delhome <家名>` | 删除指定的家 | 所有玩家 |

### 返回功能
| 指令 | 描述 | 权限 |
|------|------|------|
| `/back` | 返回最后一次死亡或传送前的位置 | 所有玩家 |

### 管理员指令
| 指令 | 描述 | 权限 |
|------|------|------|
| `/tpall` | 将所有在线玩家传送到自己位置 | OP 权限 |

## 安装方法

### 服务器端安装
1. 确保您的服务器已安装 **Fabric Loader** 和 **Fabric API**
2. 将 `FanTPA-1.0.0.jar` 放入服务器的 `mods` 文件夹
3. 启动服务器，模组会自动生成配置文件

### 客户端安装（可选）
- 如果您希望在单人游戏中使用，同样需要安装 Fabric Loader 和 Fabric API
- 将模组放入客户端的 `mods` 文件夹即可

## 配置文件

模组会在首次运行时自动生成配置文件 `config/fantpa/fantpa.conf`。

### 配置文件结构

```hocon
fantpa {
    teleport { 
        timeout = 30    # TPA 请求超时时间（秒）
        cooldown = 5    # 传送冷却时间（秒）
        delay = 3       # 传送延迟时间（秒）
    }
    language { 
        default-language = "zh_cn"  # 默认语言设置
    }
    commands {
        tpa { enabled = true }      # 启用 /tpa 指令
        tpahere { enabled = true }  # 启用 /tpahere 指令
        back { enabled = true }     # 启用 /back 指令
        home { enabled = true }     # 启用 /home 指令
        sethome { enabled = true }  # 启用 /sethome 指令
        delhome { enabled = true }  # 启用 /delhome 指令
    }
}
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `teleport.timeout` | int | 30 | TPA 请求的超时时间，超过此时间未响应则自动取消 |
| `teleport.cooldown` | int | 5 | 两次传送之间的冷却时间，防止频繁传送 |
| `teleport.delay` | int | 3 | 传送前的延迟时间，给予玩家取消传送的机会 |
| `language.default-language` | string | "en_us" | 模组的默认语言 |
| `commands.*.enabled` | boolean | true | 是否启用对应指令 |

### 修改配置
1. 停止服务器
2. 编辑 `config/fantpa/fantpa.conf` 文件
3. 保存并重启服务器

## 构建项目

### 环境要求
- Java 25 或更高版本
- Git

### 构建步骤

1. **克隆项目**
```bash
git clone https://github.com/yourusername/FanTPA.git
cd FanTPA
```

2. **构建模组**
```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

3. **获取构建产物**
构建成功后，模组文件位于 `build/libs/FanTPA-1.0.0.jar`

### 开发命令

| 命令 | 描述 |
|------|------|
| `gradlew runClient` | 启动开发客户端 |
| `gradlew runServer` | 启动开发服务器 |
| `gradlew build` | 构建模组 |
| `gradlew sourcesJar` | 生成源码 jar |

## 支持语言

| 语言代码 | 语言名称 |
|----------|----------|
| `zh_cn` | 简体中文 |
| `zh_tw` | 繁体中文 |
| `en_us` | 英语 |
| `de_de` | 德语 |
| `es_es` | 西班牙语 |
| `fr_fr` | 法语 |
| `ja_jp` | 日语 |
| `ko_kr` | 韩语 |
| `pt_br` | 葡萄牙语（巴西） |
| `ru_ru` | 俄语 |
| `it_it` | 意大利语 |
| `pl_pl` | 波兰语 |
| `tr_tr` | 土耳其语 |
| `th_th` | 泰语 |
| `vi_vn` | 越南语 |
| `uk_ua` | 乌克兰语 |

## 事件系统

模组提供了丰富的事件供其他模组监听：

- `TpaRequestEvent` - 当发送 TPA 请求时触发
- `TpaHereRequestEvent` - 当发送 TPAHere 请求时触发
- `TpaAcceptEvent` - 当接受传送请求时触发
- `TpaDenyEvent` - 当拒绝传送请求时触发
- `HomeSetEvent` - 当设置家时触发
- `HomeTeleportEvent` - 当传送回家时触发
- `BackEvent` - 当使用 `/back` 时触发
- `TeleportEvent` - 当发生传送时触发

## 技术信息

- **Mod ID**: `fantpa`
- **版本**: 1.0.0
- **Minecraft 版本**: 1.20.1+
- **依赖**: Fabric API

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！