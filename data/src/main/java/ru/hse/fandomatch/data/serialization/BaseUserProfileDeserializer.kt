package ru.hse.fandomatch.data.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import ru.hse.fandomatch.data.model.BaseUserProfileDTO
import ru.hse.fandomatch.data.model.FriendUserProfileResponseDTO
import ru.hse.fandomatch.data.model.FullUserProfileResponseDTO
import ru.hse.fandomatch.data.model.ProfileTypeDTO
import ru.hse.fandomatch.data.model.PublicUserProfileResponseDTO
import java.lang.reflect.Type

class BaseUserProfileDeserializer : JsonDeserializer<BaseUserProfileDTO> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BaseUserProfileDTO {
        val obj = json.asJsonObject
        return when (ProfileTypeDTO.fromString(obj.get("profile_type")?.asString ?: "")) {
            ProfileTypeDTO.OWN -> context.deserialize(obj, FullUserProfileResponseDTO::class.java)
            ProfileTypeDTO.FRIEND -> context.deserialize(obj, FriendUserProfileResponseDTO::class.java)
            ProfileTypeDTO.OTHER -> context.deserialize(obj, PublicUserProfileResponseDTO::class.java)
        }
    }
}
