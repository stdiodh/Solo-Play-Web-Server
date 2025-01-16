package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Course
import com.example.solo_play_web_server.course.enums.Region
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@DataR2dbcTest
@ExtendWith(SpringExtension::class)
class CourseRepositoryTest @Autowired constructor(
    private val courseRepository: CourseRepository
) : StringSpec({

    beforeTest {
        // 테스트 전에 데이터 삽입
        runBlocking {
            courseRepository.save(Course(
                userId = 1L,
                createAt = LocalDate.of(2025, 1, 1),
                like = 100,
                region = Region.downtown,
                category = "힐링자연",
                title = "Course 1",
                content = "Description of Course 1",
                place = 10L,
                post = 5L,
                review = 3L
            ))

            courseRepository.save(
                Course(
                userId = 2L,
                createAt = LocalDate.of(2025, 2, 1),
                like = 150,
                region = Region.downtown,
                category = "엑티비티",
                title = "Course 2",
                content = "Description of Course 2",
                place = 20L,
                post = 6L,
                review = 4L
            )
            )
        }
    }

    afterTest {
        // 테스트 후 데이터 삭제
        runBlocking {
            courseRepository.deleteAll()
        }
    }

    "findAllCourses 메소드로 모든 코스를 조회할 수 있다." {
        val courses = courseRepository.findAllCourses()

        courses.size shouldBe 2
        courses[0].userId shouldBe 1L
        courses[0].region shouldBe Region.downtown
        courses[1].category shouldBe "엑티비티"
        courses[1].title shouldBe "Course 2"
    }

    "findAllCourses 메소드에 틀린 정보가 들어가면 조회할 수 없다."{
        val courses = courseRepository.findAllCourses()

        courses.size shouldBe 2
        courses[0].userId shouldBe 1L
        courses[0].region shouldBe Region.downtown
        courses[1].category shouldBe "힐링자연" // 엑티비티 -> 힐링자연
        courses[1].title shouldBe "Course 2"
    }
})