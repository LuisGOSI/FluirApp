package com.example.fluirapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fluirapp.R
import com.example.fluirapp.navigation.FluirScreens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun FluirSplashScreen(navController: NavController) {
    // Animación de "brinco" para el logo
    val bounceAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Animación de rotación sutil
    val rotationAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )


    // Animación para las ondas de agua de fondo
    val waveOffset by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Animación de aparición del texto
    val textAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, delayMillis = 800)
    )

    // Animación de las gotas flotantes
    val dropAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Cálculo del efecto de brinco (movimiento vertical)
    val bounceOffset = sin(bounceAnimation * 2 * PI) * 15f

    // Marcador de tiempo para la navegación
    LaunchedEffect(key1 = true) {
        delay(3500L)
        if (FirebaseAuth.getInstance().currentUser?.email.isNullOrEmpty()){
            navController.navigate(FluirScreens.LoginScreen.name)
        }else{
            navController.navigate(FluirScreens.HomeScreen.name){
                popUpTo(FluirScreens.SplashScreen.name) {
                    inclusive = true // Elimina la pantalla de splash de la pila de navegación
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0)), // Fondo blanco
        contentAlignment = Alignment.Center
    ) {
        // Canvas para efectos de agua de fondo
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawWaterEffects(waveOffset, dropAnimation)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 100.dp)
        ) {
            // Logo principal con animaciones
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(y = bounceOffset.dp) // Efecto de brinco vertical
                    .rotate(rotationAnimation), // Rotación sutil
                contentAlignment = Alignment.Center
            ) {
                // Sombra del logo
                Image(
                    painter = painterResource(id = R.drawable.fluir_logo), // Reemplaza con el nombre de tu imagen
                    contentDescription = "Fluir Logo Shadow",
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = 6.dp, y = 6.dp)
                        .alpha(0.3f),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.4f))
                )

                // Logo principal
                Image(
                    painter = painterResource(id = R.drawable.fluir_logo), // Reemplaza con el nombre de tu imagen
                    contentDescription = "Fluir Logo",
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Texto "FLUIR" con animación
            Text(
                text = "FLUIR",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2075C6), // Azul claro
                modifier = Modifier
                    .alpha(textAlpha), // Movimiento sutil del texto
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = "Uso inteligente del agua",
                fontSize = 18.sp,
                color = Color(0xFF2075C6).copy(alpha = 0.9f),
                modifier = Modifier
                    .alpha(textAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(70.dp))

            // Indicador de carga con animación de agua
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(8.dp)
                    .background(
                        Color(0xFF2075C6).copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val progress = (waveOffset / (2 * PI)).toFloat()
                    val waveHeight = 4f
                    val path = Path()

                    // Crear onda en la barra de progreso
                    path.moveTo(0f, size.height / 2)
                    for (x in 0..(size.width * progress).toInt() step 5) {
                        val y = size.height / 2 + waveHeight * sin(x * 0.1f + waveOffset)
                        path.lineTo(x.toFloat(), y)
                    }
                    path.lineTo(size.width * progress, size.height)
                    path.lineTo(0f, size.height)
                    path.close()

                    drawPath(path, Color(0xFF0E5BA6).copy(alpha = 0.8f))
                }
            }
        }
    }
}

// Función para dibujar efectos de agua de fondo
private fun DrawScope.drawWaterEffects(waveOffset: Float, dropAnimation: Float) {
    val width = size.width
    val height = size.height

    // Ondas de fondo más sutiles
    for (i in 0..2) {
        val amplitude = 25f + i * 12f
        val frequency = 0.008f + i * 0.004f
        val yPos = height * 0.75f + i * 35f
        val alpha = 0.08f - i * 0.015f

        val path = Path()
        path.moveTo(0f, yPos)

        for (x in 0..width.toInt() step 8) {
            val y = yPos + amplitude * sin(x * frequency + waveOffset + i * PI.toFloat() / 4)
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        drawPath(path, Color(0xFF367BBC).copy(alpha = alpha))
    }
}