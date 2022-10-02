package dev.jasz.room

interface RoomRepository {

    fun get(roomId: String): Room?

}
