package com.example.fluirapp.screens.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun FluirSettingsScreen(
    navController: NavController,
    viewModel: SettingsScreenViewModel,
    onSignOut: () -> Unit
) {
    // Animaciones similares a LoginScreen
    val waveOffset by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val dropAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF367BBC).copy(alpha = 0.1f))
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Contenedor principal con el mismo estilo que Login
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF2075C6).copy(alpha = 0.3f),
                            spotColor = Color(0xFF2075C6).copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            "Configuración",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2075C6),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Línea decorativa ondulada
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                        ) {
                            val path = Path()
                            val amplitude = 8f
                            val frequency = 0.02f

                            path.moveTo(0f, size.height / 2)
                            for (x in 0..size.width.toInt() step 4) {
                                val y = size.height / 2 + amplitude * sin(x * frequency + waveOffset * 0.5f)
                                path.lineTo(x.toFloat(), y)
                            }

                            drawPath(
                                path,
                                Color(0xFF2075C6).copy(alpha = 0.6f),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Contenido de configuración
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Opciones de configuración
                            SettingsOption(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "Umbral ",
                                        tint = Color(0xFF2075C6)
                                    )
                                },
                                title = "Umbral de las bombas",
                                subtitle = "Gestiona los niveles de activación de las bombas",
                                onClick = {
                                    navController.navigate("cofiguration_tanks")
                                }
                            )

                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón de cerrar sesión
                        Button(
                            onClick = {
                                viewModel.signOut(onSignOut)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0xFFE53935).copy(alpha = 0.4f),
                                    spotColor = Color(0xFFE53935).copy(alpha = 0.4f)
                                )
                                .clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935)
                            )
                        ) {
                            Text(
                                "Cerrar sesión",
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun SettingsOption(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF2075C6).copy(alpha = 0.1f),
                spotColor = Color(0xFF2075C6).copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2075C6).copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2075C6).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Column {
                Text(
                    text = title,
                    color = Color(0xFF2075C6),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Text(
                    text = subtitle,
                    color = Color(0xFF2075C6).copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Función para dibujar efectos de agua de fondo (similar a la del login)
fun DrawScope.drawWaterEffects(waveOffset: Float, dropAnimation: Float) {
    val width = size.width
    val height = size.height

    // Ondas de fondo más sutiles
    for (i in 0..2) {
        val amplitude = 15f + i * 8f
        val frequency = 0.006f + i * 0.003f
        val yPos = height * 0.85f + i * 25f
        val alpha = 0.04f - i * 0.008f

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

    // Efectos de gotas (opcional)
    val dropCount = 3
    for (i in 0 until dropCount) {
        val x = width * (0.2f + i * 0.3f)
        val baseY = height * 0.3f
        val dropY = baseY + (height * 0.4f * ((dropAnimation + i * 0.3f) % 1f))

        drawCircle(
            color = Color(0xFF367BBC).copy(alpha = 0.1f),
            radius = 4.dp.toPx(),
            center = Offset(x, dropY)
        )
    }
}