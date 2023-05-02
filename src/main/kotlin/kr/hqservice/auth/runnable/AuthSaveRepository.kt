package kr.hqservice.auth.runnable

import kr.hqservice.auth.repository.AuthRepository

class AuthSaveRepository(
    private val authRepository: AuthRepository
) : Runnable {

    override fun run() {
        if (authRepository.isChanged) {
            authRepository.save()
        }
    }
}