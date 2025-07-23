package com.example.banditinvaders.game

import androidx.compose.ui.unit.dp
import com.example.banditinvaders.R // To refer to drawable IDs
import kotlin.random.Random

class GameEngine {
    // Constants (convert from your Java class fields)
    private val TILE = 64
    private val ROW = 16
    private val COLUMN = 16
    private var BOARD_HEIGHT: Int = 0
    private var BOARD_WIDTH: Int = 0

    // Ship
    private val SHIP_WIDTH = TILE * 2
    private val SHIP_HEIGHT = TILE
    private var SHIP_START_X: Int = 0
    // private val SHIP_START_Y = 2.0/16.0
    private val SHIP_VEL_X = TILE

    // Aliens
    private val ALIEN_WIDTH = TILE * 2
    private val ALIEN_HEIGHT = TILE
    private var ALIEN_START_X: Int = 0
    private var ALIEN_START_Y: Int = 0
    private var alienRows = 2
    private var alienColumns = 3
    private var alienVelocityX = 3 // pixels per frame
    private var alienSpawnCount = 0

    // Bullets
    private val BULLET_WIDTH = TILE / 8
    private val BULLET_HEIGHT = TILE / 2
    private val BULLET_VEL_Y = -15

    // Game State
    var ship: Block? = null
    val alienArray: MutableList<Block>
    val bulletArray: MutableList<Block>

    var score: Int = 0
        private set // Only GameEngine can set score

    var isGameOver: Boolean = false
        private set

    private var _isInitialized: Boolean = false
    fun isInitialized(): Boolean = _isInitialized

    // Alien image resource IDs (using Int for R.drawable.*)
    private val alienImageResources = listOf(
        R.drawable.alien,
        R.drawable.alien_cyan,
        R.drawable.alien_magenta,
        R.drawable.alien_yellow
    )
    fun createAliens() {
        alienArray.clear()
        bulletArray.clear()

        val random = Random

        for (row in 0 until alienRows) {
            for (col in 0 until alienColumns) {
                val alienIdx = random.nextInt(alienImageResources.size)
                val alien = Block(
                    x = ALIEN_START_X + col*ALIEN_WIDTH,
                    y = ALIEN_START_Y + row*ALIEN_HEIGHT,
                    ALIEN_WIDTH,
                    ALIEN_HEIGHT,
                    alienImageResources[alienIdx]
                )
                alienArray.add(alien)
            }
        }
        alienSpawnCount = alienArray.size
    }
    init {
        alienArray = mutableListOf()
        bulletArray = mutableListOf()
    }
    fun setBoardDimensions (width: Int, height: Int) {
        if(ship!=null && _isInitialized) {
            println("GameEngine: Dimensions already set and initialized. Skipping.")
            return
        }
        BOARD_HEIGHT = height
        BOARD_WIDTH = width
        println("GameEngine: Setting board dimensions to W:$width and H:$height")
        // setting ship start coords acc to board dimension
        SHIP_START_X = (BOARD_WIDTH / 2) - SHIP_WIDTH / 2
        val calculatedShipY = BOARD_HEIGHT - (SHIP_HEIGHT * 2)
        ship = Block(
            x = SHIP_START_X,
            y = calculatedShipY,
            width = SHIP_WIDTH,
            height = SHIP_HEIGHT,
            imgResID = R.drawable.ship
        )
        println("GameEngine: Ship created at X:${ship?.x}, Y:${ship?.y}")
        ALIEN_START_X = TILE
        ALIEN_START_Y = TILE

        // re-run all game elements (initial values)
        alienArray.clear()
        bulletArray.clear()
        score = 0
        isGameOver = false
        println("GameEngine: isGameOver set to FALSE (at setBoardDimensions)")
        alienRows = 2
        alienColumns = 3
        alienSpawnCount = 1
        createAliens()
        println("GameEngine: Created ${alienArray.size} aliens. isGameOver: $isGameOver")
        _isInitialized = true
    }
    // Call this repeatedly by the game loop
    fun update() {
        if (isGameOver || ship == null) return

        moveAliens()
        moveBullets()
        checkCollisions()
        checkGameOver()
        checkNextLevel()
        cleanUpBullets()
    }
    //fun getCurrentState() : GameState {

    //}
    private fun moveAliens() {
        // alien movement
        if (ship == null) {
            return
        }
        for (alien in alienArray) {
            if (alien.alive) {
                alien.x += alienVelocityX
            }

            if (alien.x + alien.width >= BOARD_WIDTH || alien.x <= 0) {
                alienVelocityX *= -1
                alien.x += alienVelocityX * 2 // Adjust to prevent sticking
                alienArray.forEach { aln -> aln.y += ALIEN_HEIGHT } // Move all aliens down
            }
        }
    }

    private fun moveBullets() {
        // Your existing bullet movement logic from move()
        for (bullet in bulletArray) {
            if (!bullet.used) {
                bullet.y += BULLET_VEL_Y
            }
        }
    }

    private fun checkCollisions() {
        // Your existing collision logic from move()
        val bulletsToRemove = mutableListOf<Block>()
        for (bullet in bulletArray) {
            if (bullet.used) continue // Skip already used bullets

            for (alien in alienArray) {
                if (alien.alive && detectCollision(bullet, alien)) {
                    bullet.used = true
                    alien.alive = false
                    score += 100
                    alienSpawnCount-- // Decrement count of active aliens
                    bulletsToRemove.add(bullet) // Mark for removal
                    break // Bullet hit one alien, no need to check others
                }
            }
        }
        bulletArray.removeAll(bulletsToRemove) // Remove used bullets
    }

    private fun checkGameOver() {
        for (alien in alienArray) {
            if (ship!=null && alien.alive && alien.y + ALIEN_HEIGHT >= ship!!.y) { // Alien reaches ship level
                if (!isGameOver) { // Only print if changing state
                    println("GameEngine: GAME OVER condition met! Alien Y:${alien.y + ALIEN_HEIGHT}, Ship Y:${ship!!.y}")
                }
                isGameOver = true
                break
            }
        }
    }

    private fun checkNextLevel() {
        if (alienSpawnCount == 0 && !isGameOver) {
            score += 150 // Bonus points for level clear
            // next level - inc row and col by 1
            alienColumns = Math.min(alienColumns + 1, COLUMN / 2 - 2)
            alienRows = Math.min(alienRows + 1, ROW - 6)
            alienArray.clear()
            bulletArray.clear()
            alienVelocityX = 3
            createAliens()
        }
    }

    private fun cleanUpBullets() {
        // Remove bullets that are off-screen or marked as used
        bulletArray.removeAll { it.used || it.y < 0 }
    }

    // Collision detection (your existing logic)
    private fun detectCollision(bullet: Block, alien: Block): Boolean {
        // Note: Renamed parameters for clarity for bullet vs alien
        return alien.x < bullet.x + bullet.width &&
                alien.x + alien.width > bullet.x &&
                alien.y < bullet.y + bullet.height &&
                alien.y + alien.height > bullet.y
    }

    // Input handlers (called from ViewModel)
    fun moveShipLeft() {
        if (!isGameOver && ship!=null && ship!!.x - SHIP_VEL_X >= 0) {
            ship!!.x -= SHIP_VEL_X
        }
    }

    fun moveShipRight() {
        if (!isGameOver && ship!=null && ship!!.x + SHIP_VEL_X <= BOARD_WIDTH - SHIP_WIDTH) {
            ship!!.x += SHIP_VEL_X
        }
    }

    fun shipShoot() {
        if (!isGameOver && ship!=null) {
            val bullet = Block(
                x = ship!!.x + ship!!.width * 15 / 32,
                y = ship!!.y,
                width = BULLET_WIDTH,
                height = BULLET_HEIGHT,
                //img = null // No image for bullet, just a rectangle
            )
            bulletArray.add(bullet)
        }
    }

    fun restartGame() {
        println("GameEngine: Restarting game...")
        ship!!.x = SHIP_START_X
        score = 0
        alienArray.clear()
        bulletArray.clear()
        alienVelocityX = 1
        alienColumns = 3
        alienRows = 2
        isGameOver = false
        println("GameEngine: isGameOver set to FALSE (at restartGame)")
        createAliens()
        println("GameEngine: Restart complete. New alien count: ${alienArray.size}. isGameOver: $isGameOver")
    }


    // Data class to represent the current state of the game for the UI
    data class GameState(
        val ship: Block?,
        val aliens: List<Block>, // Only alive aliens need to be drawn
        val bullets: List<Block>, // Only unused bullets need to be drawn
        val score: Int,
        val isGameOver: Boolean,
        val boardWidth: Int,
        val boardHeight: Int
    )

    fun getCurrentState(): GameState {
        if (ship == null) {
            return GameState(
                ship = null,
                aliens = emptyList(),
                bullets = emptyList(),
                score = 0,
                isGameOver = true,
                boardWidth = 0,
                boardHeight = 0
            )
        }
        return GameState(
            ship = ship!!.copy(), // Make a copy of the ship Block to capture current x,y
            aliens = alienArray.filter { it.alive }.map { it.copy() }, // Filter and map to new copies
            bullets = bulletArray.filter { !it.used }.map { it.copy() }, // Filter and map to new copies
            score = score,
            isGameOver = isGameOver,
            boardWidth = BOARD_WIDTH,
            boardHeight = BOARD_HEIGHT
        )
    }
}