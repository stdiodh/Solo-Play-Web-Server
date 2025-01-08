package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Palace
import com.example.solo_play_web_server.course.enums.Area
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient
import java.time.LocalDate

@DataR2dbcTest
class PalaceRepositoryTest @Autowired constructor(
    private val palaceRepository: PalaceRepository,
    private val databaseClient: DatabaseClient,
) : StringSpec({
    coroutineTestScope = true
    beforeTest {
        runBlocking {
            databaseClient.sql("DELETE FROM palace").then().block()
            databaseClient.sql("INSERT INTO palace (area, address, description, create_at, urls) VALUES ('downtown', 'address1', 'desc1', '2025-01-01', 'url1')").then().block()
        }
    }

    afterTest {
        clearAllMocks()
    }

    "save 메소드를 통해서 새로운 코스를 생성할 수 있다." {
        val newPalace = Palace(
            area = Area.downtown,
            address = "address1",
            description = "desc1",
            createAt = LocalDate.of(2025, 1, 2),
            urls = "url1"
        )
        val result = palaceRepository.save(newPalace)
        with(result) {
            area shouldBe Area.downtown
            address shouldBe "address1"
            description shouldBe "desc1"
            createAt shouldBe LocalDate.of(2025, 1, 2)
            urls shouldBe "url1"
        }
    }

    "deleteById 메소드를 통해 코스를 삭제할 수도 있다." {
        val newPalace = Palace(
            area = Area.downtown,
            address = "address1",
            description = "desc1",
            createAt = LocalDate.of(2025, 1, 2),
            urls = "url1"
        )
        palaceRepository.save(newPalace)

        palaceRepository.deleteById(newPalace.id!!)
        val deletePalace = palaceRepository.findById(newPalace.id!!)
        deletePalace shouldBe null
    }
})
