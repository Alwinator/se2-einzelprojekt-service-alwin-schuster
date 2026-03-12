package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.Mockito.`when` as whenever

class LeaderboardControllerTests {

    private lateinit var mockedService: GameResultService
    private lateinit var controller: LeaderboardController

    @BeforeEach
    fun setup() {
        mockedService = mock<GameResultService>()
        controller = LeaderboardController(mockedService)
    }

    @Test
    fun test_getLeaderboard_correctScoreSorting() {
        val first = GameResult(1, "first", 20, 20.0)
        val second = GameResult(2, "second", 15, 10.0)
        val third = GameResult(3, "third", 10, 15.0)

        whenever(mockedService.getGameResults()).thenReturn(listOf(second, first, third))

        val res = controller.getLeaderboard(null)

        verify(mockedService).getGameResults()
        assertEquals(3, res.size)
        assertEquals(first, res[0])
        assertEquals(second, res[1])
        assertEquals(third, res[2])
    }

    @Test
    fun test_getLeaderboard_sameScore_CorrectTimeSorting() {
        val first = GameResult(1, "first", 20, 20.0)
        val second = GameResult(2, "second", 20, 10.0)
        val third = GameResult(3, "third", 20, 15.0)

        whenever(mockedService.getGameResults()).thenReturn(listOf(second, first, third))

        val res = controller.getLeaderboard(null)

        verify(mockedService).getGameResults()
        assertEquals(3, res.size)
        assertEquals(second, res[0])
        assertEquals(third, res[1])
        assertEquals(first, res[2])
    }

    // ── rank parameter: valid cases ─────────────────────────────────────────

    @Test
    fun test_getLeaderboard_rank_returnsPlayerAndNeighbours() {
        // 7 players; ask for rank 4 (index 3) → indices 0..6 (upper 3, target, lower 3)
        val p1 = GameResult(1, "p1", 70, 1.0)
        val p2 = GameResult(2, "p2", 60, 2.0)
        val p3 = GameResult(3, "p3", 50, 3.0)
        val p4 = GameResult(4, "p4", 40, 4.0)
        val p5 = GameResult(5, "p5", 30, 5.0)
        val p6 = GameResult(6, "p6", 20, 6.0)
        val p7 = GameResult(7, "p7", 10, 7.0)

        whenever(mockedService.getGameResults()).thenReturn(listOf(p1, p2, p3, p4, p5, p6, p7))

        val res = controller.getLeaderboard(4)

        assertEquals(7, res.size)           // 3 above + target + 3 below
        assertEquals(p1, res[0])
        assertEquals(p4, res[3])            // requested rank is at correct position
        assertEquals(p7, res[6])
    }

    @Test
    fun test_getLeaderboard_rank_nearTopBoundary() {
        // rank 1: no upper neighbours, returns first 4 entries (ranks 1–4); ranks 5–10 are excluded
        val p1  = GameResult(1,  "p1",  100, 1.0)
        val p2  = GameResult(2,  "p2",  90,  2.0)
        val p3  = GameResult(3,  "p3",  80,  3.0)
        val p4  = GameResult(4,  "p4",  70,  4.0)
        val p5  = GameResult(5,  "p5",  60,  5.0)
        val p6  = GameResult(6,  "p6",  50,  6.0)
        val p7  = GameResult(7,  "p7",  40,  7.0)
        val p8  = GameResult(8,  "p8",  30,  8.0)
        val p9  = GameResult(9,  "p9",  20,  9.0)
        val p10 = GameResult(10, "p10", 10,  10.0)

        whenever(mockedService.getGameResults()).thenReturn(listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10))

        val res = controller.getLeaderboard(1)

        assertEquals(4, res.size)           // no upper neighbours: indices 0..3
        assertEquals(p1, res[0])
        assertEquals(p4, res.last())
    }

    @Test
    fun test_getLeaderboard_rank_nearBottomBoundary() {
        // rank = last (10): no lower neighbours, returns last 4 entries (ranks 7–10); ranks 1–6 are excluded
        val p1  = GameResult(1,  "p1",  100, 1.0)
        val p2  = GameResult(2,  "p2",  90,  2.0)
        val p3  = GameResult(3,  "p3",  80,  3.0)
        val p4  = GameResult(4,  "p4",  70,  4.0)
        val p5  = GameResult(5,  "p5",  60,  5.0)
        val p6  = GameResult(6,  "p6",  50,  6.0)
        val p7  = GameResult(7,  "p7",  40,  7.0)
        val p8  = GameResult(8,  "p8",  30,  8.0)
        val p9  = GameResult(9,  "p9",  20,  9.0)
        val p10 = GameResult(10, "p10", 10,  10.0)

        whenever(mockedService.getGameResults()).thenReturn(listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10))

        val res = controller.getLeaderboard(10)

        assertEquals(4, res.size)           // no lower neighbours: indices 6..9
        assertEquals(p7, res[0])
        assertEquals(p10, res.last())
    }

    // ── rank parameter: invalid cases → ResponseStatusException (HTTP 400) ─

    @Test
    fun test_getLeaderboard_rank_zero_returns_400() {
        whenever(mockedService.getGameResults()).thenReturn(listOf(GameResult(1, "p1", 10, 1.0)))

        val ex = assertFailsWith<ResponseStatusException> { controller.getLeaderboard(0) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun test_getLeaderboard_rank_negative_returns_400() {
        whenever(mockedService.getGameResults()).thenReturn(listOf(GameResult(1, "p1", 10, 1.0)))

        val ex = assertFailsWith<ResponseStatusException> { controller.getLeaderboard(-1) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun test_getLeaderboard_rank_tooLarge_returns_400() {
        whenever(mockedService.getGameResults()).thenReturn(listOf(GameResult(1, "p1", 10, 1.0)))

        val ex = assertFailsWith<ResponseStatusException> { controller.getLeaderboard(99) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

}