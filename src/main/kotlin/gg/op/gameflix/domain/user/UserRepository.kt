package gg.op.gameflix.domain.user

interface UserRepository {
    fun findById(id: Long): User?
}