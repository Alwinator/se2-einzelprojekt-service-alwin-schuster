package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.Mockito.`when` as whenever

class GameResultControllerTests {

    private lateinit var mockedService: GameResultService
    private lateinit var controller: GameResultController

    @BeforeEach
    fun setup() {
        mockedService = mock<GameResultService>()
        controller = GameResultController(mockedService)
    }

    @Test
    fun test_getGameResult_existingId_returnsGameResult() {
        val gameResult = GameResult(1, "player1", 100, 5.0)
        whenever(mockedService.getGameResult(1)).thenReturn(gameResult)

        val res = controller.getGameResult(1)

        verify(mockedService).getGameResult(1)
        assertEquals(gameResult, res)
    }

    @Test
    fun test_getGameResult_nonexistentId_returnsNull() {
        whenever(mockedService.getGameResult(99)).thenReturn(null)

        val res = controller.getGameResult(99)

        verify(mockedService).getGameResult(99)
        assertNull(res)
    }

    @Test
    fun test_getAllGameResults_returnsAllResults() {
        val list = listOf(
            GameResult(1, "player1", 100, 5.0),
            GameResult(2, "player2", 80, 7.0)
        )
        whenever(mockedService.getGameResults()).thenReturn(list)

        val res = controller.getAllGameResults()

        verify(mockedService).getGameResults()
        assertEquals(2, res.size)
        assertEquals(list[0], res[0])
        assertEquals(list[1], res[1])
    }

    @Test
    fun test_getAllGameResults_emptyList_returnsEmptyList() {
        whenever(mockedService.getGameResults()).thenReturn(emptyList())

        val res = controller.getAllGameResults()

        verify(mockedService).getGameResults()
        assertEquals(emptyList<GameResult>(), res)
    }

    @Test
    fun test_addGameResult_delegatesToService() {
        val gameResult = GameResult(0, "player1", 100, 5.0)

        controller.addGameResult(gameResult)

        verify(mockedService).addGameResult(gameResult)
    }

    @Test
    fun test_deleteGameResult_delegatesToService() {
        controller.deleteGameResult(1)

        verify(mockedService).deleteGameResult(1)
    }
}

