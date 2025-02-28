import com.example.solo_play_web_server.course.entity.Place
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class PlaceRequestDTO(
    @field:NotEmpty(message = "이름을 입력해주세요")
    val name: String,

    @field:NotEmpty(message = "지역을 입력해주세요")
    val region: String,

    @field:NotEmpty(message = "설명을 입력해주세요")
    val description: String,

    @field:NotNull(message = "사진 URL이 올바르지 않습니다!")
    val urls: List<String>
) {
    fun toEntity(): Place {
        return Place(
            name = this.name,
            region = this.region,
            description = this.description,
            urls = this.urls
        )
    }
}


data class PlaceResponseDTO(
    val id: String?,
    val name: String,
    val region: String,
    val description: String,
    val urls: List<String>
)

