package gg.op.gameflix.domain.user

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class User(
    @Id
    var id: String,
    var email: String
)


