import com.example.solo_play_web_server.common.annotation.ValidEnum
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.Region
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class PlaceRequestDTO(
    @field:NotEmpty(message = "이름을 입력해주세요")
    @JsonProperty("name")
    private val _name: String,

    @field:NotEmpty(message = "지역을 입력해주세요")
    @field:ValidEnum(enumClass = Region::class, message = "잘못된 지역 입니다.")
    @JsonProperty("region")
    private val _region: String,

    @field:NotEmpty(message = "설명을 입력해주세요")
    @JsonProperty("description")
    private val _description: String,

    @field:NotNull(message = "사진 URL이 올바르지 않습니다!")
    @field:Size(min = 1, message = "URL 리스트는 최소 1개 이상의 항목을 가져야 합니다.")
    @JsonProperty("urls")
    private val _urls: List<String>
) {
    val name : String
        get() = _name!!
    val region : Region
        get() = Region.valueOf(_region!!)
    val description : String
        get() = _description
    val urls : List<String>
        get() = _urls


    fun toEntity(): Place {
        return Place(
            id = null,
            name = name,
            region = region,
            description = description,
            urls = urls
        )
    }
}


data class PlaceResponseDTO(
    val id: String?,
    val name: String,
    val region: Region,
    val description: String,
    val urls: List<String>
)

