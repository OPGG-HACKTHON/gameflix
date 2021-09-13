# User store authentication

## GOG
[Authentication — GOG-API 0.1 documentation](https://gogapidocs.readthedocs.io/en/latest/auth.html)
- Pass access token when POST /users/{id}/stores with authentication field in json body

Query Parameters
- client_id (str) – OAuth2 Client ID. Use 46899977096215655.
- redirect_uri (str) – URL where the browser will be redirected after the login has been completed. Use https://embed.gog.com/on_login_success?origin=client.
- response_type (str) – Use code
- layout (str) – Use client2

## Blizzard
[OAuth APIs | Documentation](https://develop.battle.net/documentation/battle-net/oauth-apis)
- Pass access token when POST /users/{id}/stores with authentication field in json body
- client id: 916b6064383441388fa56d2b3af3779a
- client secert:  RrKJsOMXTn7AVxhFyscX8ABQiF9Ja9nw

## Steam
- Pass steam id when POST /users/{id}/stores with authentication field in json body
- Can be found in [Steam's account](https://store.steampowered.com/account/)

### reference
- [steam-user - npm](https://www.npmjs.com/package/steam-user)
- [skhamis/steam-game-picker: Allows you to use OpenID to sign in to your steam account and randomly choose games from your steam library using only React hooks](https://github.com/skhamis/steam-game-picker)