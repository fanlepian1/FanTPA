# FanTPA

A powerful Minecraft Fabric teleportation mod that provides TPA, Home, Back and other teleportation features.

---

**[ÖÐÎÄ°æ±¾](README_zh.md)** | [English](README.md)

## Features

### TPA Teleportation System
| Command | Description | Permission |
|---------|-------------|------------|
| `/tpa <player>` | Send teleport request to another player | All players |
| `/tpahere <player>` | Request another player to teleport to you | All players |
| `/tpaccept <player>` | Accept teleport request | All players |
| `/tpdeny <player>` | Deny teleport request | All players |

### Home System
| Command | Description | Permission |
|---------|-------------|------------|
| `/home [name]` | Teleport to your home (default home if no name specified) | All players |
| `/sethome [name]` | Set current location as home (default home if no name) | All players |
| `/delhome <name>` | Delete specified home | All players |

### Back Feature
| Command | Description | Permission |
|---------|-------------|------------|
| `/back` | Return to last death or teleport location | All players |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/tpall` | Teleport all online players to your location | OP only |

## Installation

### Server Installation
1. Ensure your server has **Fabric Loader** and **Fabric API** installed
2. Place `FanTPA-1.0.0.jar` into the server's `mods` folder
3. Start the server, the mod will automatically generate config files

### Client Installation (Optional)
- If you want to use in single player, install Fabric Loader and Fabric API as well
- Place the mod in client's `mods` folder

## Configuration File

The mod will automatically generate a config file at `config/fantpa/fantpa.conf` on first run.

### Config Structure

```hocon
fantpa {
    teleport { 
        timeout = 30    # TPA request timeout in seconds
        cooldown = 5    # Teleport cooldown in seconds
        delay = 3       # Teleport delay in seconds
    }
    language { 
        default-language = "en_us"  # Default language setting
    }
    commands {
        tpa { enabled = true }      # Enable /tpa command
        tpahere { enabled = true }  # Enable /tpahere command
        back { enabled = true }     # Enable /back command
        home { enabled = true }     # Enable /home command
        sethome { enabled = true }  # Enable /sethome command
        delhome { enabled = true }  # Enable /delhome command
    }
}
```

### Config Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `teleport.timeout` | int | 30 | TPA request timeout, auto-cancelled if not responded |
| `teleport.cooldown` | int | 5 | Cooldown between teleports to prevent spamming |
| `teleport.delay` | int | 3 | Delay before teleportation, allows canceling |
| `language.default-language` | string | "en_us" | Default language for the mod |
| `commands.*.enabled` | boolean | true | Enable/disable specific commands |

### How to Modify Config
1. Stop the server
2. Edit `config/fantpa/fantpa.conf` file
3. Save and restart the server

## Building from Source

### Requirements
- Java 25 or higher
- Git

### Build Steps

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/FanTPA.git
cd FanTPA
```

2. **Build the mod**
```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

3. **Get the output**
Built jar file will be located at `build/libs/FanTPA-1.0.0.jar`

### Development Commands

| Command | Description |
|---------|-------------|
| `gradlew runClient` | Launch development client |
| `gradlew runServer` | Launch development server |
| `gradlew build` | Build the mod |
| `gradlew sourcesJar` | Generate sources jar |

## Supported Languages

| Code | Language |
|------|----------|
| `zh_cn` | Chinese (Simplified) |
| `zh_tw` | Chinese (Traditional) |
| `en_us` | English |
| `de_de` | German |
| `es_es` | Spanish |
| `fr_fr` | French |
| `ja_jp` | Japanese |
| `ko_kr` | Korean |
| `pt_br` | Portuguese (Brazil) |
| `ru_ru` | Russian |
| `it_it` | Italian |
| `pl_pl` | Polish |
| `tr_tr` | Turkish |
| `th_th` | Thai |
| `vi_vn` | Vietnamese |
| `uk_ua` | Ukrainian |

## Event System

The mod provides rich events for other mods to listen:

- `TpaRequestEvent` - Fired when a TPA request is sent
- `TpaHereRequestEvent` - Fired when a TPAHere request is sent
- `TpaAcceptEvent` - Fired when a teleport request is accepted
- `TpaDenyEvent` - Fired when a teleport request is denied
- `HomeSetEvent` - Fired when a home is set
- `HomeTeleportEvent` - Fired when teleporting home
- `BackEvent` - Fired when using `/back`
- `TeleportEvent` - Fired when a teleport occurs

## Technical Information

- **Mod ID**: `fantpa`
- **Version**: 1.0.0
- **Minecraft Version**: 1.20.1+
- **Dependency**: Fabric API

## License

MIT License

## Contributing

Contributions are welcome! Feel free to submit issues and pull requests.