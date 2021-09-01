package gg.op.gameflix.domain.user

interface UserRepository {
    fun findById(id: String): User?
    fun save(entity: User): User
}