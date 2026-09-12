A plugin for Velocity that detects connections via **proxy, VPN, and hosting** and blocks such players at the login
stage

## Features

- IP check at the `PreLogin` stage through a selected provider
- Blocks connections from proxies, VPNs, and hosting
- Stores blocked players in a SQLite database
- Discord webhook notifications with embed messages on block
- `/proxyguard` command (alias `/pg`) for plugin management
- Fully configurable messages and settings via `config.yml`

## Commands

All commands require the `proxyguard.admin` permission

| Command                                                 | Description                       |
|---------------------------------------------------------|-----------------------------------|
| `/proxyguard reload`                                    | Reload the configuration          |
| `/proxyguard users`                                     | List all blocked players          |
| `/proxyguard unban <uuid>`                              | Unban a player by UUID            |
| `/proxyguard settings provider <name>`                  | Switch the check provider         |
| `/proxyguard settings discord enable`                   | Enable Discord notifications      |
| `/proxyguard settings discord disable`                  | Disable Discord notifications     |
| `/proxyguard settings discord webhook <url>`            | Set the webhook URL               |
| `/proxyguard settings discord embed title <text>`       | Set the embed title               |
| `/proxyguard settings discord embed description <text>` | Set the embed description         |
| `/proxyguard settings discord embed reason <text>`      | Set the block reason in the embed |

## IP providers

The provider is set in `config.yml` (`provider`)

| Value         | Service                                 | API key                         | Notes                                                             |
|---------------|-----------------------------------------|---------------------------------|-------------------------------------------------------------------|
| `IP_API`      | [ip-api.com](https://ip-api.com/)       | not required                    | Fields `proxy`, `hosting`                                         |
| `VPN_API`     | [vpnapi.io](https://vpnapi.io/)         | required (`vpn-api-key`)        | Fields `security.vpn`, `security.proxy`                           |
| `PROXY_CHECK` | [proxycheck.io](https://proxycheck.io/) | required (`proxycheck-api-key`) | Fields `detections.vpn`, `detections.proxy`, `detections.hosting` |

Each request is performed asynchronously with a 5-second timeout; on error or API unavailability the plugin login flow
is not blocked

## Discord notifications

When a player is blocked, if `discord.enabled` is enabled in the config, an embed is sent to the configured webhook with
the following fields:

- **Player** — nickname of the blocked player
- **Reason** — block reason
- **IP** — IP address (hidden behind a spoiler)

The embed title, description, and reason can be customized via the config or commands

> All text messages support the [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) format

## License

The project is distributed under the [GPL-3.0](LICENSE) license. Authors: n3vvx, k1llm3sixy