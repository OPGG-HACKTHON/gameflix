# User store authentication

## GOG
[Authentication — GOG-API 0.1 documentation](https://gogapidocs.readthedocs.io/en/latest/auth.html)
- Pass access token when POST /users/{id}/stores with authentication field in json body

## Blizzard
[OAuth APIs | Documentation](https://develop.battle.net/documentation/battle-net/oauth-apis)
- Pass access token when POST /users/{id}/stores with authentication field in json body

## Steam
- Pass steam id when POST /users/{id}/stores with authentication field in json body
- Can be found in [Steam's account](https://store.steampowered.com/account/)

### reference
- [steam-user - npm](https://www.npmjs.com/package/steam-user)
- [skhamis/steam-game-picker: Allows you to use OpenID to sign in to your steam account and randomly choose games from your steam library using only React hooks](https://github.com/skhamis/steam-game-picker)