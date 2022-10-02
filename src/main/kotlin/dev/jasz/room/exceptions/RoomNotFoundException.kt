package dev.jasz.room.exceptions

data class RoomNotFoundException(val roomId: String) : Exception("Room with id $roomId not found")
