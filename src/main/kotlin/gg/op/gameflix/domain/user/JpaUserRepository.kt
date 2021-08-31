package gg.op.gameflix.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository : JpaRepository<User, Long>, UserRepository {
}