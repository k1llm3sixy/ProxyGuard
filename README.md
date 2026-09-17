> Detects connections via **proxy and VPN** and blocks such players

## Features

- IP check through a selected provider
- Blocks connections from proxies and VPNs
- Stores blocked players
- Caches verified users
- IP whitelist to bypass the check
- Blocks can be logged via Discord webhook
- Fully configurable messages and settings via config

## Commands

`proxyguard.admin` grants access to all commands. Alternatively, each command can be granted individually:

| Command                             | Description                     | Permission              |
|-------------------------------------|---------------------------------|-------------------------|
| `/proxyguard reload`                | Reload config                   | `proxyguard.reload`     |
| `/proxyguard users`                 | List all blocked players        | `proxyguard.users`      |
| `/proxyguard check <ip>`            | Check an IP for proxy/VPN usage | `proxyguard.check`      |
| `/proxyguard stats`                 | Show stats                      | `proxyguard.stats`      |
| `/proxyguard unban <uuid>`          | Unban a player by UUID          | `proxyguard.unban`      |
| `/proxyguard whitelist add <ip>`    | Add an IP to the whitelist      | `proxyguard.whitelist`  |
| `/proxyguard whitelist remove <ip>` | Remove an IP from the whitelist | `proxyguard.whitelist`  |
| `/proxyguard whitelist list`        | List all whitelisted IPs        | `proxyguard.whitelist`  |

## IP providers

The provider is set in `config`

| Value         | Service                                 |
|---------------|-----------------------------------------|
| `VPN_API`     | [vpnapi.io](https://vpnapi.io/)         |
| `PROXY_CHECK` | [proxycheck.io](https://proxycheck.io/) |

## Discord notifications

When a player is blocked, if `discord.enabled` is enabled in the config, an embed is sent to the configured webhook with
the following fields:

- **Player** - nickname of the blocked player
- **Detection** - detected connection type (PROXY or VPN)
- **IP** - IP address

> The embed title, description, field names, and values can be customized via the config

> All text messages support only [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) format