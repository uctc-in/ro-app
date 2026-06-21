package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val childName: String = "𐴌𐴋𐴞𐴓𐴔𐴝 𐴔𐴝𐴃𐴜", // Rohingya title/name placeholder
    val age: Int = 6,
    val coins: Int = 0,
    val stars: Int = 0,
    val trophies: Int = 0,
    val badgesString: String = "", // e.g. "counting_star,addition_hero"
    val parentPin: String = "𐴱𐴲𐴳𐴴", // Rohingya default pin "1234" -> 𐴱𐴲𐴳𐴴 in Hanifi
    val dailyChallengeCompletedDate: String = "" // "YYYY-MM-DD"
)

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, // Addition, Counting, Multi, Shape, Time, Money, Measurement
    val levelAgeGroup: String, // "5-6", "7-8", "9-10", "11-12"
    val problemsAttempted: Int,
    val problemsCorrect: Int,
    val scorePercentage: Int,
    val timeSpentSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_certificates")
data class SavedCertificate(
    @PrimaryKey val certificateId: String, // e.g. "RO-MATH-5892"
    val childName: String,
    val age: Int,
    val levelName: String, // Beginner, Explorer, Champion, Math Hero, Math Master
    val score: Int,
    val earnedDate: String,
    val qrContent: String
)

@Dao
interface MathDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgressFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressDirect(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProgress(progress: UserProgress)

    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getGameHistoryFlow(): Flow<List<GameHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistory(history: GameHistory)

    @Query("SELECT * FROM saved_certificates ORDER BY earnedDate DESC")
    fun getAllCertificatesFlow(): Flow<List<SavedCertificate>>

    @Query("SELECT * FROM saved_certificates WHERE certificateId = :certId")
    suspend fun getCertificateById(certId: String): SavedCertificate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(cert: SavedCertificate)

    @Query("DELETE FROM game_history")
    suspend fun clearHistory()

    @Query("DELETE FROM saved_certificates")
    suspend fun clearCertificates()
}

@Database(entities = [UserProgress::class, GameHistory::class, SavedCertificate::class], version = 1, exportSchema = false)
abstract class MathDatabase : RoomDatabase() {
    abstract fun mathDao(): MathDao
}
