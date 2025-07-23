package com.example.banditinvaders.userinterface

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banditinvaders.game.GameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.cancel

class GameViewModel : ViewModel(){
    private val gameEngine = GameEngine()

    // use game state (updates everything each frame)
    private val _gameState = MutableStateFlow(GameEngine.GameState(
        ship = null,
        aliens = emptyList(),
        bullets = emptyList(),
        score = 0,
        isGameOver = true,
        boardWidth = 0,
        boardHeight = 0
    ))
    val gameState: StateFlow<GameEngine.GameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null    // game loop

//    init {
//        startGameLoop()
//    }
    fun setGameSize (width: Int, height: Int) {
        println("GameViewModel: setGameAreaSize called with W:$width H:$height")
        if (gameEngine.ship == null || !gameEngine.isInitialized()) {
            gameEngine.setBoardDimensions(width, height)
            _gameState.value = gameEngine.getCurrentState()
        }
        if (!gameEngine.isGameOver && (gameLoopJob == null || !gameLoopJob!!.isActive)) {
            startGameLoop()  // start game looop only if no other running
            println("GameViewModel: Starting game loop. Current isGameOver: ${gameEngine.isGameOver}")
        }else {
            println("GameViewModel: Game loop not started. isGameOver: ${gameEngine.isGameOver}, job active: ${gameLoopJob?.isActive}")
        }
        //_gameState.value = gameEngine.getCurrentState() // update game state after setting new dimensions
    }
    private fun startGameLoop() {
        gameLoopJob?.cancel()  // stop any existing game loop (if any)
        gameLoopJob = viewModelScope.launch {  // start new game loop
            println("GameLoop: Coroutine launched.")
            while (isActive) {
                println("GameLoop: Updated frame. Score: ${_gameState.value.score}, Aliens: ${_gameState.value.aliens.size}, isGameOver: ${_gameState.value.isGameOver}")
                gameEngine.update()
                _gameState.value = gameEngine.getCurrentState()
                if (gameEngine.isGameOver) {
                    println("GameLoop: Game over detected, breaking loop.")
                    gameLoopJob?.cancel()
                }
                delay(1000L / 16)  // = 16.6ms ie approx 60FPS
            }
            println("GameLoop: Coroutine finished. isGameOver: ${gameEngine.isGameOver}")
        }
    }

    fun moveShipRight() {
        gameEngine.moveShipRight()
    }

    fun moveShipLeft() {
        gameEngine.moveShipLeft()
    }

    fun shipShoot() {
        gameEngine.shipShoot()
    }

    fun restartGame() {
        println("GameViewModel: restartGame called.")
        gameEngine.restartGame()
        _gameState.value = gameEngine.getCurrentState()
        println("GameViewModel: State updated after restart. isGameOver: ${_gameState.value.isGameOver}")
        startGameLoop()
        println("GameViewModel: startGameLoop called after restart.")
    }

    // when view model will no longer be used
    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()  // stop any running game loop
    }
}