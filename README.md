A plugin for Velocity that detects connections via **proxy and VPN** and blocks such players at the login stage

## Features

- IP check at the `PreLogin` stage through a selected provider
- Blocks connections from proxies and VPNs
- Stores blocked players in a SQLite database
- Caches verified users so repeated connections don't hit the provider API again
- IP whitelist to bypass the check
- Blocks can be logged via Discord webhook with embed messages
- `/proxyguard` command (alias `/pg`) for plugin management
- Fully configurable messages and settings via `config.yml`
- Anonymous usage statistics via bStats

## Commands

All commands require the `proxyguard.admin` permission

| Command                                                 | Description                       |
|---------------------------------------------------------|-----------------------------------|
| `/proxyguard reload`                                    | Reload the configuration          |
| `/proxyguard users`                                     | List all blocked players          |
| `/proxyguard unban <uuid>`                              | Unban a player by UUID            |
| `/proxyguard whitelist add <ip>`                        | Add an IP to the whitelist        |
| `/proxyguard whitelist remove <ip>`                     | Remove an IP from the whitelist   |
| `/proxyguard whitelist list`                            | List all whitelisted IPs          |

## IP providers

The provider is set in `config.yml` (`provider`)

| Value         | Service                                 | API key                         | Notes                                       |
|---------------|-----------------------------------------|---------------------------------|---------------------------------------------|
| `VPN_API`     | [vpnapi.io](https://vpnapi.io/)         | required (`vpn-api-key`)        | Fields `security.vpn`, `security.proxy`     |
| `PROXY_CHECK` | [proxycheck.io](https://proxycheck.io/) | required (`proxycheck-api-key`) | Fields `detections.vpn`, `detections.proxy` |

Each request is performed asynchronously with a 5-second timeout; on error or API unavailability the plugin login flow
is not blocked

## Discord notifications

When a player is blocked, if `discord.enabled` is enabled in the config, an embed is sent to the configured webhook with
the following fields:

- **Player** — nickname of the blocked player
- **Reason** — block reason
- **IP** — IP address (hidden behind a spoiler)

The embed title, description, and reason can be customized via the config

> All text messages support the [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) format

## License

The project is distributed under the [GPL-3.0](LICENSE) license. Authors: n3vvx, k1llm3sixy