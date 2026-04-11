package ru.hse.fandomatch.data.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import ru.hse.fandomatch.data.model.BaseUserProfileDTO
import ru.hse.fandomatch.data.model.FriendUserProfileResponseDTO
import ru.hse.fandomatch.data.model.FullUserProfileResponseDTO
import ru.hse.fandomatch.data.model.PublicUserProfileResponseDTO
import java.lang.reflect.Type

class BaseUserProfileDeserializer : JsonDeserializer<BaseUserProfileDTO> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BaseUserProfileDTO {
        val obj = json.asJsonObject
        return when (val type = obj.get("profile_type")?.asString) {
            "OWN" -> context.deserialize(obj, FullUserProfileResponseDTO::class.java)
            "FRIEND" -> context.deserialize(obj, FriendUserProfileResponseDTO::class.java)
            "OTHER" -> context.deserialize(obj, PublicUserProfileResponseDTO::class.java)
            else -> throw JsonParseException("Unknown profile_type: $type")
        }
    }
}
