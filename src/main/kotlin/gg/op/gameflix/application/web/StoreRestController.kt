package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.Store
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/stores")
@RestController
class StoreRestController {

    @GetMapping
    fun getStores(): MultipleStoreModel =
        Store.values()
            .map { createStoreModel(it) }
            .let { storeModels -> MultipleStoreModel(storeModels) }

    @GetMapping("/{id}")
    fun getStoresById(@PathVariable id: String): StoreModel =
        id.replace("-", "_")
            .uppercase()
            .let { Store.valueOf(it) }
            .let { store -> createStoreModel(store) }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalStoreValueException(exception: Exception) {
        // thrown if GET /stores/{id} has invalid id
    }

    private fun createStoreModel(store: Store): StoreModel
        = when(store) {
            Store.STEAM -> StoreModel(Store.STEAM.name.lowercase(), "Pass steam id of user in this field when POST \"/users/{user-id}/stores\"")
            Store.GOG -> StoreModel(Store.GOG.name.lowercase(), "Pass API KEY of user in this field when POST \"/users/{user-id}/stores\"")
            Store.BLIZZARD -> StoreModel(Store.BLIZZARD.name.lowercase(), "Pass API KEY of user in this field when POST \\\"/users/{user-id}/stores\\\"")
    }
}

@Suppress("kotlin:S117")
data class StoreModel(
    val slug: String,
    val authentication: String
)

data class MultipleStoreModel(
    val stores: List<StoreModel>
)