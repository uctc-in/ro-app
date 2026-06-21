package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.GameHistory
import com.example.data.database.SavedCertificate
import com.example.data.database.UserProgress
import com.example.data.repository.MathRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Format helper extension to convert numbers and characters to Hanifi script
fun String.toHanifi(): String {
    val builder = StringBuilder()
    for (char in this) {
        when (char) {
            '0' -> builder.append("𐴰")
            '1' -> builder.append("𐴱")
            '2' -> builder.append("𐴲")
            '3' -> builder.append("𐴳")
            '4' -> builder.append("𐴴")
            '5' -> builder.append("𐴵")
            '6' -> builder.append("𐴶")
            '7' -> builder.append("𐴷")
            '8' -> builder.append("𐴸")
            '9' -> builder.append("𐴹")
            else -> builder.append(char)
        }
    }
    return builder.toString()
}

fun Int.toHanifi(): String = this.toString().toHanifi()

enum class GameType(val label: String, val rawName: String) {
    LEARN_NUMBERS("Learn Numbers", "Numbers 1 to 100"),
    COUNTING("Counting Match", "Objects & Fruits"),
    ADDITION("Addition (+)", "Summing Numbers"),
    SUBTRACTION("Subtraction (-)", "Subtracting Numbers"),
    MULTIPLICATION("Multiplication (×)", "Times Tables"),
    DIVISION("Division (÷)", "Sharing Items"),
    FRACTIONS("Fractions", "Cake Slices"),
    SHAPES("Shapes", "Geometry & Corners"),
    TIME_CLOCK("Time Clock", "Reading Hours"),
    MONEY_COUNTING("Money Math", "Taka Coins"),
    MEASUREMENT("Measurement", "Lengths & Heights"),
    DAILY_CHALLENGE("Daily Challenge", "Mixed Math Practice")
}

data class MathProblem(
    val titleRo: String, // Instruction in Hanifi wording
    val visualType: String, // "apples", "balloons", "candies", "clock", "shapes", "money", "race", "factions", "text"
    val visualCount1: Int = 0,
    val visualCount2: Int = 0,
    val operand1: String = "",
    val operand2: String = "",
    val operatorSymbol: String = "",
    val customClockHour: Int = 12,
    val customClockMinute: Int = 0,
    val shapeType: String = "", // "circle", "triangle", "square", "rectangle", "star"
    val fractionNumerator: Int = 0,
    val fractionDenominator: Int = 0,
    val moneyBills: List<Int> = emptyList(), // currency denoms: 5, 10, 20, 50
    val options: List<String>, // standard or fraction/word choices in Hanifi digits
    val correctIndex: Int,
    val explanationRo: String = ""
)

class MathViewModel(
    application: Application,
    private val repository: MathRepository
) : AndroidViewModel(application) {

    // Sound effects Generator
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (e: Exception) {
        null
    }

    var playSoundEffects = true

    // State flows
    val userProgress: StateFlow<UserProgress> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.Lazily, UserProgress())

    val gameHistory: StateFlow<List<GameHistory>> = repository.gameHistoryList
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val certificates: StateFlow<List<SavedCertificate>> = repository.certificates
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Game states
    private val _currentScreen = MutableStateFlow("home") // "home", "game", "parent", "cert_verify", "daily_challenge"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _activeGameType = MutableStateFlow<GameType?>(null)
    val activeGameType: StateFlow<GameType?> = _activeGameType.asStateFlow()

    // Active score/session track
    private val _problemsSolved = MutableStateFlow(0)
    val problemsSolved = _problemsSolved.asStateFlow()

    private val _problemsTotal = MutableStateFlow(0)
    val problemsTotal = _problemsTotal.asStateFlow()

    private val _gameCoinsBonus = MutableStateFlow(0)
    val gameCoinsBonus = _gameCoinsBonus.asStateFlow()

    private val _gameStarsBonus = MutableStateFlow(0)
    val gameStarsBonus = _gameStarsBonus.asStateFlow()

    private val _currentProblem = MutableStateFlow<MathProblem?>(null)
    val currentProblem = _currentProblem.asStateFlow()

    // Answer evaluation
    private val _answerStatus = MutableStateFlow<Boolean?>(null) // true if correct, false if wrong, null outstanding
    val answerStatus = _answerStatus.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex = _selectedAnswerIndex.asStateFlow()

    // Animal race track positions (0 to 10f)
    private val _rabbitProgress = MutableStateFlow(1f)
    val rabbitProgress = _rabbitProgress.asStateFlow()
    private val _elephantProgress = MutableStateFlow(1f)
    val elephantProgress = _elephantProgress.asStateFlow()

    // For Daily Challenge tracking
    private val _dailyChallengeState = MutableStateFlow<String>("not_started") // "not_started", "active", "completed_today"
    val dailyChallengeState = _dailyChallengeState.asStateFlow()

    init {
        // Evaluate initial daily status
        viewModelScope.launch {
            repository.userProgress.collect { progress ->
                val today = getTodayDateString()
                if (progress.dailyChallengeCompletedDate == today) {
                    _dailyChallengeState.value = "completed_today"
                } else {
                    _dailyChallengeState.value = "not_started"
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        _answerStatus.value = null
        _selectedAnswerIndex.value = null
    }

    fun startNewGame(gameType: GameType) {
        _activeGameType.value = gameType
        _problemsSolved.value = 0
        _problemsTotal.value = 0
        _gameCoinsBonus.value = 0
        _gameStarsBonus.value = 0
        resetAnimalProgress()
        generateNextProblem()
        navigateTo("game")
    }

    fun resetAnimalProgress() {
        _rabbitProgress.value = 1f
        _elephantProgress.value = 1f
    }

    fun playBeep(correct: Boolean) {
        if (!playSoundEffects) return
        try {
            if (correct) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            } else {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            }
        } catch (_: Exception) {}
    }

    fun selectAnswer(index: Int) {
        val problem = _currentProblem.value ?: return
        if (_answerStatus.value != null) return // Already evaluated

        _selectedAnswerIndex.value = index
        val isCorrect = index == problem.correctIndex
        _answerStatus.value = isCorrect
        playBeep(isCorrect)

        if (isCorrect) {
            _problemsSolved.value = _problemsSolved.value + 1
            _gameCoinsBonus.value = _gameCoinsBonus.value + 4
            _gameStarsBonus.value = _gameStarsBonus.value + 3

            // Speed up the rabbit on the race
            _rabbitProgress.value = (_rabbitProgress.value + 1.8f).coerceAtMost(10f)
        } else {
            // Speed up elephant opponent instead
            _elephantProgress.value = (_elephantProgress.value + 1.5f).coerceAtMost(10f)
        }
        _problemsTotal.value = _problemsTotal.value + 1
    }

    fun skipOrNext() {
        _answerStatus.value = null
        _selectedAnswerIndex.value = null
        if (_problemsTotal.value >= 10 || (_activeGameType.value == GameType.DAILY_CHALLENGE && _problemsTotal.value >= 5)) {
            // Screen completion
            completeSession()
        } else {
            generateNextProblem()
        }
    }

    fun completeSession() {
        val ageGroup = when (userProgress.value.age) {
            in 5..6 -> "5-6"
            in 7..8 -> "7-8"
            in 9..10 -> "9-10"
            else -> "11-12"
        }
        val typeLabel = _activeGameType.value?.rawName ?: "Math"
        
        viewModelScope.launch {
            val isDaily = _activeGameType.value == GameType.DAILY_CHALLENGE
            val total = _problemsTotal.value
            val solved = _problemsSolved.value
            val percentage = if (total > 0) (solved * 100) / total else 0

            // Call repository
            repository.recordGamePlay(
                gameType = typeLabel,
                levelGroup = ageGroup,
                attempted = total,
                correct = solved,
                timeSpentSec = 120, // Estimated play average
                gainedCoins = _gameCoinsBonus.value,
                gainedStars = _gameStarsBonus.value,
                gainedTrophy = percentage >= 80
            )

            if (isDaily && percentage >= 60) {
                repository.completeDailyChallenge(percentage, getTodayDateString(), 25)
                _dailyChallengeState.value = "completed_today"
            }

            // Automatic Certificate Generation if they reached Math Master/Hero criteria
            if (percentage >= 90) {
                checkAndGenerateCertificate(typeLabel, percentage)
            }

            navigateTo("game_over")
        }
    }

    private suspend fun checkAndGenerateCertificate(game: String, score: String) {
        // Checked in UI triggered or background progress
    }

    fun issueManualCertificate(levelName: String, score: Int) {
        viewModelScope.launch {
            val progress = userProgress.value
            val randomId = "RO-MATH-${Random.nextInt(1000, 9999)}"
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val newCert = SavedCertificate(
                certificateId = randomId,
                childName = progress.childName,
                age = progress.age,
                levelName = levelName,
                score = score,
                earnedDate = todayStr,
                qrContent = "Verified Math for Ro certificate. Student: ${progress.childName}, Level: $levelName, Score: $score%"
            )
            repository.issueCertificate(newCert)
        }
    }

    fun checkAndGenerateCertificate(levelName: String, score: Int) {
        issueManualCertificate(levelName, score)
    }

    fun generateNextProblem() {
        val game = _activeGameType.value ?: GameType.COUNTING
        val age = userProgress.value.age
        _currentProblem.value = buildProc    private fun buildProceduralProblem(gameType: GameType, age: Int): MathProblem {
        val rand = Random.Default
        return when (gameType) {
            GameType.LEARN_NUMBERS -> {
                // Number learning is interactive matching and recognition
                val targetNum = rand.nextInt(1, 15)
                MathProblem(
                    titleRo = "Find the correct number: ${targetNum}!", // Match the digit
                    visualType = "apples",
                    visualCount1 = targetNum,
                    options = listOf(targetNum.toHanifi(), (targetNum + 1).toHanifi(), (targetNum + 2).coerceAtMost(30).toHanifi()).shuffled(),
                    correctIndex = 0, // Options are shuffled, but correct is calculated correctly below
                    explanationRo = "${targetNum} matches Rohingya digit: ${targetNum.toHanifi()}"
                ).calcCorrectIndex(targetNum.toHanifi())
            }
            GameType.COUNTING -> {
                val fruitCount = if (age <= 6) rand.nextInt(1, 10) else rand.nextInt(5, 20)
                val visualOptions = listOf("apples", "balloons", "candies", "toys").random()
                MathProblem(
                    titleRo = "How many $visualOptions are there in total?", // How many objects in total?
                    visualType = visualOptions,
                    visualCount1 = fruitCount,
                    options = generateOptions(fruitCount, 0, 30),
                    correctIndex = 0,
                    explanationRo = "Count them! In total there are $fruitCount (${fruitCount.toHanifi()})"
                ).calcCorrectIndex(fruitCount.toHanifi())
            }
            GameType.ADDITION -> {
                val (n1, n2) = if (age <= 6) {
                    Pair(rand.nextInt(1, 7), rand.nextInt(1, 4))
                } else if (age <= 8) {
                    Pair(rand.nextInt(10, 50), rand.nextInt(5, 30))
                } else {
                    Pair(rand.nextInt(50, 400), rand.nextInt(40, 300))
                }
                val ans = n1 + n2
                MathProblem(
                    titleRo = "Solve: ${n1} + ${n2}?", // Add items
                    visualType = if (age <= 6) "balloons" else "text",
                    visualCount1 = if (age <= 6) n1 else 0,
                    visualCount2 = if (age <= 6) n2 else 0,
                    operand1 = n1.toHanifi(),
                    operand2 = n2.toHanifi(),
                    operatorSymbol = "+",
                    options = generateOptions(ans, 1, 1000),
                    correctIndex = 0,
                    explanationRo = "${n1} + ${n2} = ${ans} (${n1.toHanifi()} + ${n2.toHanifi()} = ${ans.toHanifi()})"
                ).calcCorrectIndex(ans.toHanifi())
            }
            GameType.SUBTRACTION -> {
                val (n1, n2) = if (age <= 6) {
                    val first = rand.nextInt(4, 10)
                    Pair(first, rand.nextInt(1, first))
                } else if (age <= 8) {
                    val first = rand.nextInt(20, 70)
                    Pair(first, rand.nextInt(5, first - 5))
                } else {
                    val first = rand.nextInt(100, 500)
                    Pair(first, rand.nextInt(20, first - 10))
                }
                val ans = n1 - n2
                MathProblem(
                    titleRo = "Solve: ${n1} - ${n2}?", // Subtract items
                    visualType = if (age <= 6) "candies" else "text",
                    visualCount1 = if (age <= 6) n1 else 0,
                    visualCount2 = if (age <= 6) n2 else 0,
                    operand1 = n1.toHanifi(),
                    operand2 = n2.toHanifi(),
                    operatorSymbol = "-",
                    options = generateOptions(ans, 0, 1000),
                    correctIndex = 0,
                    explanationRo = "${n1} - ${n2} = ${ans} (${n1.toHanifi()} - ${n2.toHanifi()} = ${ans.toHanifi()})"
                ).calcCorrectIndex(ans.toHanifi())
            }
            GameType.MULTIPLICATION -> {
                val (n1, n2) = if (age <= 6) {
                    Pair(rand.nextInt(1, 5), rand.nextInt(1, 3)) // Visual multi
                } else if (age <= 8) {
                    Pair(rand.nextInt(1, 6), rand.nextInt(1, 5)) // tables
                } else if (age <= 10) {
                    Pair(rand.nextInt(5, 12), rand.nextInt(3, 10))
                } else {
                    Pair(rand.nextInt(12, 45), rand.nextInt(5, 15))
                }
                val ans = n1 * n2
                MathProblem(
                    titleRo = "What is ${n1} × ${n2}?", // multiply
                    visualType = if (age <= 8) "toys" else "text",
                    visualCount1 = if (age <= 8) n1 else 0,
                    visualCount2 = if (age <= 8) n2 else 0,
                    operand1 = n1.toHanifi(),
                    operand2 = n2.toHanifi(),
                    operatorSymbol = "×",
                    options = generateOptions(ans, 1, 1000),
                    correctIndex = 0,
                    explanationRo = "${n1} × ${n2} = ${ans} (${n1.toHanifi()} × ${n2.toHanifi()} = ${ans.toHanifi()})"
                ).calcCorrectIndex(ans.toHanifi())
            }
            GameType.DIVISION -> {
                val divisor = if (age <= 8) rand.nextInt(2, 5) else rand.nextInt(4, 12)
                val quotient = if (age <= 8) rand.nextInt(1, 5) else rand.nextInt(3, 15)
                val dividend = divisor * quotient
                MathProblem(
                    titleRo = "Divide: ${dividend} ÷ ${divisor}?", // divide
                    visualType = "text",
                    operand1 = dividend.toHanifi(),
                    operand2 = divisor.toHanifi(),
                    operatorSymbol = "÷",
                    options = generateOptions(quotient, 1, 100).map { it.toHanifi() },
                    correctIndex = 0,
                    explanationRo = "${dividend} ÷ ${divisor} = ${quotient} (${dividend.toHanifi()} ÷ ${divisor.toHanifi()} = ${quotient.toHanifi()})"
                ).calcCorrectIndex(quotient.toHanifi())
            }
            GameType.FRACTIONS -> {
                val denom = listOf(2, 3, 4, 6, 8, 10).filter { age >= 9 || it <= 4 }.random()
                val numer = rand.nextInt(1, denom)
                MathProblem(
                    titleRo = "What fraction of the cake is shaded?", // What fraction is shaded?
                    visualType = "fractions",
                    fractionNumerator = numer,
                    fractionDenominator = denom,
                    options = listOf(
                        "${numer.toHanifi()}/${denom.toHanifi()}",
                        "${(numer + 1).coerceAtMost(denom - 1).toHanifi()}/${denom.toHanifi()}",
                        "𐴱/${(denom + 1).toHanifi()}"
                    ).distinct(),
                    correctIndex = 0,
                    explanationRo = "Shaded fraction matches: ${numer}/${denom} (${numer.toHanifi()}/${denom.toHanifi()})"
                ).calcCorrectIndex("${numer.toHanifi()}/${denom.toHanifi()}")
            }
            GameType.SHAPES -> {
                val shapes = listOf("circle", "triangle", "square", "rectangle", "star", "heart")
                val targetShape = shapes.random()
                val shapesEnglish = mapOf(
                    "circle" to "Circle 🔴",
                    "triangle" to "Triangle 🔺",
                    "square" to "Square 🟩",
                    "rectangle" to "Rectangle ▮",
                    "star" to "Star ⭐",
                    "heart" to "Heart ❤️"
                )
                val targetName = shapesEnglish[targetShape] ?: "Shape"
                MathProblem(
                    titleRo = "Which of the options is a $targetName?", // Which shape is this?
                    visualType = "shapes",
                    shapeType = targetShape,
                    options = shapes.map { shapesEnglish[it] ?: "" }.shuffled(),
                    correctIndex = 0,
                    explanationRo = "That's right! It is a $targetName!"
                ).calcCorrectIndex(targetName)
            }
            GameType.TIME_CLOCK -> {
                val hour = rand.nextInt(1, 13)
                val minute = if (age <= 8) 0 else listOf(0, 15, 30, 45).random()
                val minStr = if (minute == 0) "𐴰𐴰" else minute.toString().toHanifi()
                val correctTime = "${hour.toHanifi()}:$minStr"
                
                MathProblem(
                    titleRo = "Read the clock! What time is it?", // What is the correct time?
                    visualType = "clock",
                    customClockHour = hour,
                    customClockMinute = minute,
                    options = listOf(
                        correctTime,
                        "${((hour + 1) % 12 + 1).toHanifi()}:𐴰𐴰",
                        "${hour.toHanifi()}:${((minute + 30) % 60).let { if(it==0) "𐴰𐴰" else it.toString().toHanifi() }}"
                    ).distinct(),
                    correctIndex = 0,
                    explanationRo = "The clock hand points to $hour:${String.format("%02d", minute)} ($correctTime)"
                ).calcCorrectIndex(correctTime)
            }
            GameType.MONEY_COUNTING -> {
                // Generate a set of bills
                val bills = if (age <= 8) {
                    List(rand.nextInt(1, 4)) { listOf(5, 10).random() }
                } else {
                    List(rand.nextInt(2, 5)) { listOf(10, 20, 50).random() }
                }
                val totalMoney = bills.sum()
                MathProblem(
                    titleRo = "Count the Taka bills! What is the total?", // How much money?
                    visualType = "money",
                    moneyBills = bills,
                    options = generateOptions(totalMoney, 5, 500),
                    correctIndex = 0,
                    explanationRo = "Count equal to $totalMoney Taka (${totalMoney.toHanifi()} 𐴃𐴝𐴑𐴝)"
                ).calcCorrectIndex(totalMoney.toHanifi())
            }
            GameType.MEASUREMENT -> {
                // Comparing bar lengths
                val bar1 = rand.nextInt(3, 10)
                val bar2 = bar1 + listOf(-2, -1, 1, 2).random().let { if (bar1 + it in 3..10) it else 1 }
                val isBigger = bar1 > bar2
                val targetQ = if (rand.nextBoolean()) "longer" else "shorter"
                val isLongerQ = targetQ == "longer"
                val correctAns = if ((isLongerQ && bar1 > bar2) || (!isLongerQ && bar1 < bar2)) "𐴱" else "𐴲"

                MathProblem(
                    titleRo = "Which of the colored bars is $targetQ?", // Which one is longer/shorter?
                    visualType = "measurement",
                    visualCount1 = bar1,
                    visualCount2 = bar2,
                    options = listOf("𐴱", "𐴲"),
                    correctIndex = if (correctAns == "𐴱") 0 else 1,
                    explanationRo = "Bar 1 has height ${bar1} and Bar 2 has height ${bar2}."
                )
            }
            GameType.DAILY_CHALLENGE -> {
                // Mixed procedural problems based on age!
                val randomGames = listOf(GameType.ADDITION, GameType.SUBTRACTION, GameType.MULTIPLICATION)
                buildProceduralProblem(randomGames.random(), age)
            }
        }
    }���𐴔𐴝"
                val correctAns = if ((isLongerQ && bar1 > bar2) || (!isLongerQ && bar1 < bar2)) "𐴱" else "𐴲"

                MathProblem(
                    titleRo = "𐴑𐴡𐴃𐴠 𐴓𐴠𐴔𐴝𐴃𐴠𐴓: $targetQ?", // Which one is longer/shorter?
                    visualType = "measurement",
                    visualCount1 = bar1,
                    visualCount2 = bar2,
                    options = listOf("𐴱", "𐴲"),
                    correctIndex = if (correctAns == "𐴱") 0 else 1,
                    explanationRo = ""
                )
            }
            GameType.DAILY_CHALLENGE -> {
                // Mixed procedural problems based on age!
                val randomGames = listOf(GameType.ADDITION, GameType.SUBTRACTION, GameType.MULTIPLICATION)
                buildProceduralProblem(randomGames.random(), age)
            }
        }
    }

    private fun generateOptions(answer: Int, min: Int, max: Int): List<String> {
        val options = mutableSetOf<String>()
        options.add(answer.toHanifi())
        while (options.size < 3) {
            val deviation = Random.nextInt(-10, 10).let { if (it == 0) 2 else it }
            val fake = (answer + deviation).coerceIn(min, max)
            options.add(fake.toHanifi())
        }
        return options.toList().shuffled()
    }

    private fun MathProblem.calcCorrectIndex(correctString: String): MathProblem {
        val computedIndex = options.indexOf(correctString).let { if (it == -1) 0 else it }
        return this.copy(correctIndex = computedIndex)
    }

    fun modifyProfile(name: String, age: Int) {
        viewModelScope.launch {
            val current = repository.getOrInitializeUserProgress()
            val updated = current.copy(childName = name, age = age.coerceIn(5, 12))
            repository.updateUserProgress(updated)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetAllData()
            _problemsSolved.value = 0
            _problemsTotal.value = 0
            _gameCoinsBonus.value = 0
            _gameStarsBonus.value = 0
            navigateTo("home")
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
