package model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostEntity (
    @PrimaryKey val id: Int,
    val authorId: Int,
    val createdAt: String,
    val contentPicture: String,
    val contentText: String?,
    @Embedded val location: LocationPostEntity? = null

)

data class LocationPostEntity (
    val latitude: Double,
    val longitude: Double
)

fun Post.toEntity(): PostEntity{
    return PostEntity(
        id = id,
        authorId = authorId,
        createdAt = createdAt,
        contentPicture = contentPicture,
        contentText = contentText,
        location = location?.let {
            if (it.latitude != null && it.longitude != null) {
                LocationPostEntity(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            } else {
                null
            }
        }
    )
}

fun PostEntity.toPost(): Post {
    return Post(
        id = id,
        authorId = authorId,
        createdAt = createdAt,
        contentPicture = contentPicture,
        contentText = contentText,
        location = location?.let {
            LocationData(
                latitude = it.latitude,
                longitude = it.longitude
            )
        }
    )
}