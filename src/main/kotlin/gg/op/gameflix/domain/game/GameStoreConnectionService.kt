package gg.op.gameflix.domain.game

import gg.op.gameflix.domain.user.User

class GameStoreConnectionService(val stores: Collection<GameStoreService>) {

    fun connectUserWithStore(user: User, auth: GameStoreAuthentication): User {
        TODO("Not yet implemented")
    }
}