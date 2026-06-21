package com.example.data.repository

import com.example.data.database.GameHistory
import com.example.data.database.MathDao
import com.example.data.database.SavedCertificate
import com.example.data.database.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MathRepository(private val mathDao: MathDao) {

    // Expose flow of user progress, always ensuring it initializes if null
    val userProgress: Flow<UserProgress> = mathDao.getUserProgressFlow().map {
        it ?: UserProgress().also { defaultProgress ->
            // Seed default progress on a background coroutine
            mathDao.insertOrUpdateUserProgress(defaultProgress)
        }
    }

    val gameHistoryList: Flow<List<GameHistory>> = mathDao.getGameHistoryFlow()

    val certificates: Flow<List<SavedCertificate>> = mathDao.getAllCertificatesFlow()

    suspend fun getOrInitializeUserProgress(): UserProgress {
        var progress = mathDao.getUserProgressDirect()
        if (progress == null) {
            progress = UserProgress()
            mathDao.insertOrUpdateUserProgress(progress)
        }
        return progress
    }

    suspend fun updateUserProgress(progress: UserProgress) {
        mathDao.insertOrUpdateUserProgress(progress)
    }

    suspend fun recordGamePlay(
        gameType: String,
        levelGroup: String,
        attempted: Int,
        correct: Int,
        timeSpentSec: Int,
        gainedCoins: Int,
        gainedStars: Int,
        gainedTrophy: Boolean
    ) {
        // Record History
        val percentage = if (attempted > 0) (correct * 100) / attempted else 0
        val history = GameHistory(
            gameType = gameType,
            levelAgeGroup = levelGroup,
            problemsAttempted = attempted,
            problemsCorrect = correct,
            scorePercentage = percentage,
            timeSpentSeconds = timeSpentSec
        )
        mathDao.insertGameHistory(history)

        // Update User progress
        val currentProgress = getOrInitializeUserProgress()
        val updatedBadges = updateBadgesOnPerformance(currentProgress, gameType, correct, percentage)
        
        val updatedProgress = currentProgress.copy(
            coins = currentProgress.coins + gainedCoins,
            stars = currentProgress.stars + gainedStars,
            trophies = currentProgress.trophies + (if (gainedTrophy) 1 else 0),
            badgesString = updatedBadges
        )
        mathDao.insertOrUpdateUserProgress(updatedProgress)
    }

    private fun updateBadgesOnPerformance(
        current: UserProgress,
        gameType: String,
        correct: Int,
        percentage: Int
    ): String {
        val badges = current.badgesString.split(",").filter { it.isNotEmpty() }.toMutableSet()
        
        // Add badges based on criteria
        if (percentage == 100 && correct >= 5) {
            badges.add("${gameType.lowercase()}_master")
        }
        if (current.stars >= 50 && !badges.contains("star_collector")) {
            badges.add("star_collector")
        }
        if (current.coins >= 100 && !badges.contains("coin_rich")) {
            badges.add("coin_rich")
        }
        if (current.trophies >= 3 && !badges.contains("trophy_collector")) {
            badges.add("trophy_collector")
        }

        return badges.joinToString(",")
    }

    suspend fun issueCertificate(certificate: SavedCertificate) {
        mathDao.insertCertificate(certificate)
    }

    suspend fun getCertificate(certId: String): SavedCertificate? {
        return mathDao.getCertificateById(certId)
    }

    suspend fun completeDailyChallenge(percentage: Int, dateStr: String, coinsReward: Int) {
        val current = getOrInitializeUserProgress()
        if (current.dailyChallengeCompletedDate != dateStr) {
            val updated = current.copy(
                coins = current.coins + coinsReward,
                stars = current.stars + 15,
                dailyChallengeCompletedDate = dateStr
            )
            mathDao.insertOrUpdateUserProgress(updated)
        }
    }

    suspend fun resetAllData() {
        mathDao.clearHistory()
        mathDao.clearCertificates()
        mathDao.insertOrUpdateUserProgress(UserProgress(id = 1))
    }
}
