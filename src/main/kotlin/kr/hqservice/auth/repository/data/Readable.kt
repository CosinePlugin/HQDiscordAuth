package kr.hqservice.auth.repository.data

interface Readable<T> {

    fun read(data: T)
}