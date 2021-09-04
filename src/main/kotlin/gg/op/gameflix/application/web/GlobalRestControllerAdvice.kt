package gg.op.gameflix.application.web

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalRestControllerAdvice {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException() {
        // do nothing
    }
}