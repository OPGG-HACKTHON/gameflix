package gg.op.gameflix.domain.user

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id

@Entity
class User(id: String) {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: String? = null

}

