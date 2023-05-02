package kr.hqservice.auth.repository.data

interface Writable<T> {

    fun write(data: T)
}