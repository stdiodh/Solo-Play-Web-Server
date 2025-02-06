package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Course
import com.example.solo_play_web_server.course.entity.CoursePlace
import com.example.solo_play_web_server.course.entity.Place
import com.example.solo_play_web_server.course.enums.Region
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@DataR2dbcTest
@ExtendWith(SpringExtension::class)
class CourseRepositoryTest @Autowired constructor(
    private val courseRepository: CourseRepository,
    private val placeRepository: PlaceRepository,
    private val coursePlaceRepository: CoursePlaceRepository
) : StringSpec({

    beforeTest {
        runBlocking {
            // 장소 2개 저장
            val place1 = placeRepository.save(Place(id = null, name = "Place 1", region = Region.downtown, description = "Description of Place 1", urls = "url1"))
            val place2 = placeRepository.save(Place(id = null, name = "Place 2", region = Region.downtown, description = "Description of Place 2", urls = "url2"))

            // 코스 저장
            val course = courseRepository.save(
                Course(
                    userId = 1L,
                    createAt = LocalDate.of(2025, 1, 1),
                    like = 100,
                    region = Region.downtown,
                    category = "힐링자연",
                    title = "Course 1",
                    content = "Description of Course 1",
                    post = 5L,
                    review = 3L
                )
            )

            // 코스와 장소 연결 (중간 테이블)
            coursePlaceRepository.save(CoursePlace(courseId = course.id!!, placeId = place1.id!!))
            coursePlaceRepository.save(CoursePlace(courseId = course.id!!, placeId = place2.id!!))
        }
    }

    afterTest {
        runBlocking {
            // 테스트 데이터 삭제
            coursePlaceRepository.deleteAll()
            courseRepository.deleteAll()
            placeRepository.deleteAll()
        }
    }

    "코스에 연결된 장소 ID를 올바르게 조회할 수 있다." {
        runBlocking {
            val course = courseRepository.findAll().first()
            val placeIds = coursePlaceRepository.findPlacesByCourseId(course.id!!)

            placeIds.size shouldBe 2 // 장소 2개가 저장되어 있어야 함
        }
    }

    "존재하지 않는 코스 ID로 장소 조회할 때 빈 리스트를 반환해야 한다." {
        val invalidCourseId = 999L // 존재하지 않는 ID
        val places = coursePlaceRepository.findPlacesByCourseId(invalidCourseId)

        places shouldBe emptyList() // 빈 리스트가 반환되어야 함
    }

    "존재하지 않는 장소를 코스에 추가하려 하면 데이터가 저장되지 않아야 한다." {
        val invalidPlaceId = 999L // 존재하지 않는 장소 ID
        val validCourseId = 1L // 존재하는 코스 ID

        runBlocking {
            // 중간 테이블에 추가 시도
            coursePlaceRepository.save(CoursePlace(courseId = validCourseId, placeId = invalidPlaceId))

            // 저장된 데이터 확인
            val places = coursePlaceRepository.findPlacesByCourseId(validCourseId)

            // 존재하지 않는 장소 ID는 저장되지 않아야 함
            places shouldNotContain invalidPlaceId
        }
    }

    "findTop10RecommendedCourses는 좋아요 수가 가장 많은 10개의 코스를 반환해야 한다." {
        runBlocking {
            // 15개의 코스를 저장 (좋아요 수를 다르게 설정)
            val courses = (1..15).map {
                courseRepository.save(
                    Course(
                        userId = it.toLong(),
                        createAt = LocalDate.of(2025, 1, 1),
                        like = it * 10, // 좋아요 수 증가
                        region = Region.downtown,
                        category = "카테고리$it",
                        title = "Course $it",
                        content = "Content $it",
                        post = it.toLong(),
                        review = it.toLong()
                    )
                )
            }

            // 상위 10개의 코스 가져오기
            val top10Courses = courseRepository.findTop10RecommendedCourses()
            // 코스 개수 확인 (최대 10개)
            top10Courses.size shouldBe 10
            // 좋아요 수 내림차순 정렬 확인
            top10Courses.map { it.like } shouldBe top10Courses.map { it.like }.sortedDescending()
        }
    }

    "findTopLikedCoursesToday는 오늘 날짜의 코스 중 좋아요가 많은 순서대로 반환해야 한다." {
        runBlocking {
            val today = LocalDate.now()

            // 오늘 생성된 코스 추가
            val course1 = courseRepository.save(
                Course(
                    userId = 1L,
                    createAt = today,
                    like = 200,
                    region = Region.downtown,
                    category = "힐링",
                    title = "오늘 코스 1",
                    content = "오늘 코스 내용 1",
                    post = 10L,
                    review = 5L
                )
            )

            val course2 = courseRepository.save(
                Course(
                    userId = 2L,
                    createAt = today,
                    like = 300,
                    region = Region.gangnam,
                    category = "엑티비티",
                    title = "오늘 코스 2",
                    content = "오늘 코스 내용 2",
                    post = 12L,
                    review = 6L
                )
            )

            // 어제 날짜의 코스 추가 (조회되면 안 됨)
            courseRepository.save(
                Course(
                    userId = 3L,
                    createAt = today.minusDays(1),
                    like = 500,
                    region = Region.gangbuk,
                    category = "자연",
                    title = "어제 코스",
                    content = "어제 코스 내용",
                    post = 15L,
                    review = 7L
                )
            )

            // 오늘 생성된 코스 중 좋아요가 많은 순으로 가져오기
            val topLikedToday = courseRepository.findTopLikedCoursesToday(today)
            // 조회된 코스 개수 확인 (어제 날짜 코스 제외)
            topLikedToday.size shouldBe 2
            // 포함된 코스가 오늘 날짜인지 확인
            topLikedToday.all { it.createAt == today } shouldBe true
            // 좋아요 수 내림차순 확인
            topLikedToday.map { it.like } shouldBe listOf(300, 200)
        }
    }
})
