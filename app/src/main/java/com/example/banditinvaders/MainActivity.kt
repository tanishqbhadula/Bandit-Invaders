package com.example.banditinvaders

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.banditinvaders.ui.theme.BanditInvadersTheme
import com.example.banditinvaders.userinterface.GameViewModel
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas // drawText on Canvas
import androidx.compose.ui.graphics.nativeCanvas // drawText on Canvas
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity  // for toPx()
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BanditInvadersTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val gameViewModel: GameViewModel = GameViewModel()
                    // val gameState by gameViewModel.gameState.collectAsState()
                    // Text("Score : ${gameState.score} | Game Over : ${gameState.isGameOver}")
                    GameScreen(gameViewModel)
                }
            }

        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()  // for text composables
    val density = LocalDensity.current
    val canvasSize = remember { mutableStateOf(IntSize(0, 0)) }
    // loading ship img as bitmap (remembered)
    val shipBitmap : ImageBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ship).asImageBitmap()
    }

    val alienImgBitmap : Map<Int,ImageBitmap> = remember {
        mapOf(
            R.drawable.alien to BitmapFactory.decodeResource(context.resources, R.drawable.alien).asImageBitmap(),
            R.drawable.alien_cyan to BitmapFactory.decodeResource(context.resources, R.drawable.alien_cyan).asImageBitmap(),
            R.drawable.alien_yellow to BitmapFactory.decodeResource(context.resources, R.drawable.alien_yellow).asImageBitmap(),
            R.drawable.alien_magenta to BitmapFactory.decodeResource(context.resources, R.drawable.alien_magenta).asImageBitmap()
        )
    }

    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            with(density) {
                textSize = 32.sp.toPx()
            }
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    // buttons
    val dPadImage = painterResource(id = R.drawable.d_pad) // Load D-pad image
    val buttonAImage = painterResource(id = R.drawable.a_button) // A button - SHOOT
    val buttonBImage = painterResource(id = R.drawable.b_button) // B button - RESTART

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)  // general padding all around
            .background(androidx.compose.ui.graphics.Color.DarkGray)
            .padding(WindowInsets.systemBars.asPaddingValues())  // padding for task bar
            .border(2.dp,color = androidx.compose.ui.graphics.Color.Black)
    ) {

        // game play area = 2/3 of screen
        Box(
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Color.Black)
                .fillMaxWidth()
                .weight(2f)
                .padding(4.dp)
                .border(2.dp,color = androidx.compose.ui.graphics.Color.LightGray)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        viewModel.setGameSize(size.width, size.height)
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                offset: Offset -> viewModel.shipShoot()
                            }
                        )
                    }
            ) {
                // draw a ship img
                gameState.ship?.let { currentShip ->
                    drawImage(
                        image = shipBitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(
                            width = shipBitmap.width,
                            height = shipBitmap.height
                        ),
                        dstOffset = IntOffset(x = currentShip.x, y = currentShip.y),
                        dstSize = IntSize(
                            width = currentShip.width,
                            height = currentShip.height
                        )
                    )
                }

                // draw aliens
                gameState.aliens.forEach { alien ->
                    alien.imgResID?.let { resID ->
                        alienImgBitmap[resID]?.let { alienBitmap ->
                            drawImage(
                                image = alienBitmap,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(width = alienBitmap.width, height = alienBitmap.height),
                                dstOffset = IntOffset(x = alien.x, y = alien.y),
                                dstSize = IntSize(width = alien.width, height = alien.height)
                            )
                        }
                    }
                }

                // draw bullets
                gameState.bullets.forEach { bullet ->
                    drawRect(
                        color = androidx.compose.ui.graphics.Color.White,
                        topLeft = Offset(x = bullet.x.toFloat(), y = bullet.y.toFloat()),
                        size = Size(width = bullet.width.toFloat(), height = bullet.height.toFloat())
                    )
                }

                // disp score
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        gameState.score.toString(),
                        10.dp.toPx(), // x = 10
                        35.dp.toPx(), // y = 35
                        textPaint
                    )
                }
            }

            // game over disp overlay
            if (gameState.isGameOver) {
                Column (
                    modifier = Modifier
                        .fillMaxSize() // fill the entire play area
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)), // translucent bg
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GAME OVER : ${gameState.score}",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = "Press RESTART button to play again",
//                        color = androidx.compose.ui.graphics.Color.White,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Normal,
//                        fontFamily = FontFamily.Monospace
//                    )
                }
            }
        } // game screen box

        // control buttons area 1/3 of screen
//        Column (
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth()
//                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
//                .padding(16.dp),
//            verticalArrangement = Arrangement.SpaceEvenly,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // row 1 -> left and right mvmt buttons
//            Row (
//                modifier = Modifier
//                    .fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                Button(  // LEFT
//                    onClick = {viewModel.moveShipLeft()},
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(8.dp),
//                    enabled = !gameState.isGameOver // disable movement when game is over
//                ) {
//                    Text(
//                        text = "LEFT",
//                        fontSize = 20.sp
//                    )
//                }
//                Button(  // RIGHT
//                    onClick = {viewModel.moveShipRight()},
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(8.dp),
//                    enabled = !gameState.isGameOver
//                ) {
//                    Text(
//                        text = "RIGHT",
//                        fontSize = 20.sp
//                    )
//                }
//            }
//
//            // row 2 : shoot and restart
//            Row (
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Button(  // SHOOT
//                    onClick = {viewModel.shipShoot()},
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(8.dp),
//                    enabled = !gameState.isGameOver
//                ) {
//                    Text(
//                        text = "SHOOT",
//                        fontSize = 20.sp
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Button(  // RESTART
//                    onClick = {viewModel.restartGame()},
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(8.dp),
//                    //enabled = !gameState.isGameOver
//                ) {
//                    Text(
//                        text = "RESTART",
//                        fontSize = 20.sp
//                    )
//                }
//            }
//        } // control buttons area
        // control buttons image
        Row (
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(androidx.compose.ui.graphics.Color.DarkGray)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // D_PAD LEFT
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = dPadImage,
                    contentDescription = "D_PAD",
                    modifier = Modifier
                        .size(120.dp)
                )

                val dPadAreaSize = 120.dp // total size of image
                val dPadButtonSize = 40.dp // size of each arrow button
                val dPadOffsetFromCentre = 30.dp // dist of arrow button from centre of dpad

                // LEFT ARROW
                val leftInteractionSource = remember { MutableInteractionSource() }
                val isLeftPressed by leftInteractionSource.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .size(dPadAreaSize)
                        .offset(x = -dPadOffsetFromCentre, y = 0.dp)
                        .align(Alignment.Center)
                        .clickable (
                            enabled = !gameState.isGameOver,
                            interactionSource = leftInteractionSource,
                            indication = null
                        ) {
                            viewModel.moveShipLeft()
                        }
                )
                // RIGHT ARROW
                val rightInteractionSource = remember { MutableInteractionSource() }
                val isRightPressed by rightInteractionSource.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .size(dPadAreaSize)
                        .offset(x = dPadOffsetFromCentre, y = 0.dp)
                        .align(Alignment.Center)
                        .clickable (
                            enabled = !gameState.isGameOver,
                            interactionSource = rightInteractionSource,
                            indication = null
                        ) {
                            viewModel.moveShipRight()
                        }
                )
            }
            Spacer(modifier = Modifier.width(32.dp))

            // A and B buttons
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // A button (SHOOT)
                val aButtonInteractionSource = remember { MutableInteractionSource() }
                val isAPressed by aButtonInteractionSource.collectIsPressedAsState()
                Image (
                    painter = buttonAImage,
                    contentDescription = "Shoot Button (A)",
                    modifier = Modifier
                        .size(80.dp)
                        .clickable (
                            enabled = !gameState.isGameOver,
                            interactionSource = aButtonInteractionSource,
                            indication = null
                        ) {
                            viewModel.shipShoot()
                        }
                        .background(if (isAPressed) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent)

                )

                Spacer(modifier = Modifier.height(16.dp))

                // B button (RESTART)
                val bButtonInteractionSource = remember { MutableInteractionSource() }
                val isBPressed by bButtonInteractionSource.collectIsPressedAsState()

                Image (
                    painter = buttonBImage,
                    contentDescription = "Restart Button (B)",
                    modifier = Modifier
                        .size(80.dp)
                        .clickable (
                            interactionSource = bButtonInteractionSource,
                            indication = null
                        ) {
                            viewModel.restartGame()
                        }
                        .background(if (isBPressed) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
    LaunchedEffect(canvasSize.value) { // This effect runs whenever canvasSize.value changes
        if (canvasSize.value.width > 0 && canvasSize.value.height > 0) { // Ensure dimensions are valid
            viewModel.setGameSize(canvasSize.value.width, canvasSize.value.height)
            println("GameScreen: LaunchedEffect called setGameAreaSize with W:${canvasSize.value.width} H:${canvasSize.value.height}")
        }
    }
}
