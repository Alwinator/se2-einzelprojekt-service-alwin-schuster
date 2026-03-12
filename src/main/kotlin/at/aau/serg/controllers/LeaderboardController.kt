package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
    private val gameResultService: GameResultService
) {

    @GetMapping
    fun getLeaderboard(@RequestParam(required = false) rank: Int?): List<GameResult> {
        val sorted = gameResultService.getGameResults()
            .sortedWith(compareBy({ -it.score }, { it.timeInSeconds }))

        if (rank == null) {
            return sorted
        }

        // rank is 1-based; validate against the sorted list size
        if (rank < 1 || rank > sorted.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Rank must be between 1 and ${sorted.size}")
        }

        val index = rank - 1 // convert to 0-based index
        val fromIndex = maxOf(0, index - 3)
        val toIndex = minOf(sorted.size - 1, index + 3)

        return sorted.subList(fromIndex, toIndex + 1)
    }

}