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

> All commands require the `proxyguard.admin` permission

| Command                             | Description                     |
|-------------------------------------|---------------------------------|
| `/proxyguard reload`                | Reload config                   |
| `/proxyguard users`                 | List all blocked players        |
| `/proxyguard unban <uuid>`          | Unban a player by UUID          |
| `/proxyguard whitelist add <ip>`    | Add an IP to the whitelist      |
| `/proxyguard whitelist remove <ip>` | Remove an IP from the whitelist |
| `/proxyguard whitelist list`        | List all whitelisted IPs        |

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
- **Reason** - block reason
- **IP** - IP address

> The embed title, description, and reason can be customized via the config

> All text messages support only [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) format