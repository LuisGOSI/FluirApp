package com.example.fluirapp.screens.login

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fluirapp.navigation.FluirScreens
import kotlin.math.*

@Composable
fun FluirLoginScreen(
    navController: NavController,
    viewModel: LoginScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val showLoginForm = rememberSaveable {
        mutableStateOf(true)
    }

    // Animaciones de fondo similares a SplashScreen
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

    // Animación sutil para el contenido principal
    val floatAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val contentOffset = sin(floatAnimation * 2 * PI) * 3f

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
                    .offset(y = contentOffset.dp) // Flotación sutil
                    .padding(horizontal = 24.dp)
            ) {

                Spacer(modifier = Modifier.height(40.dp))

                // Contenedor principal con sombra y bordes redondeados
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
                        if (showLoginForm.value) {
                            Text(
                                "Inicia sesión",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2075C6),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                "Crea una cuenta",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2075C6),
                                textAlign = TextAlign.Center
                            )
                        }

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

                        if (showLoginForm.value) {
                            UserForm(
                                isCreateAccount = false
                            ) { email, password ->
                                viewModel.signInWithEmailAndPassword(email, password) {
                                    navController.navigate(FluirScreens.HomeScreen.name)
                                }
                            }
                        } else {
                            UserForm(
                                isCreateAccount = true
                            ) { email, password ->
                                Log.d("FluirApp", "Creando cuenta con email: $email")
                                viewModel.createUserWithEmailAndPassword(email, password) {
                                    navController.navigate(FluirScreens.HomeScreen.name)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val text1 =
                                if (showLoginForm.value) "¿No tienes una cuenta? " else "¿Ya tienes una cuenta? "
                            val text2 = if (showLoginForm.value) "Crear cuenta" else "Iniciar sesión"

                            Text(
                                text = text1,
                                color = Color(0xFF2075C6).copy(alpha = 0.8f)
                            )
                            Text(
                                text = text2,
                                modifier = Modifier
                                    .clickable { showLoginForm.value = !showLoginForm.value }
                                    .padding(start = 5.dp),
                                color = Color(0xFF0E5BA6),
                                fontWeight = FontWeight.SemiBold
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
fun UserForm(
    isCreateAccount: Boolean = false,
    onDone: (String, String) -> Unit = {email, pwd ->}
) {
    val email = rememberSaveable{
        mutableStateOf("")
    }
    val password = rememberSaveable{
        mutableStateOf("")
    }
    val passwordVisible = rememberSaveable{
        mutableStateOf(false)
    }
    val valido = remember(email.value, password.value){
        email.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(horizontalAlignment = Alignment.CenterHorizontally){
        EmailInput(
            emailState = email
        )
        PasswordInput(
            passwordState = password,
            labelId = "Contraseña",
            passwordVisible = passwordVisible
        )
        SubmitButton(
            textId = if(isCreateAccount) "Crear Cuenta" else "Iniciar Sesión",
            inputValido = valido
        ){
            onDone(email.value.trim(), password.value.trim())
            keyboardController?.hide()
        }
    }
}

@Composable
fun SubmitButton(
    textId: String,
    inputValido: Boolean,
    onClic: () -> Unit
) {
    Button(
        onClick = onClic,
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth()
            .shadow(
                elevation = if (inputValido) 8.dp else 2.dp,
                shape = CircleShape,
                ambientColor = Color(0xFF2075C6).copy(alpha = 0.4f),
                spotColor = Color(0xFF2075C6).copy(alpha = 0.4f)
            ),
        shape = CircleShape,
        enabled = inputValido,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2075C6),
            disabledContainerColor = Color(0xFF2075C6).copy(alpha = 0.3f)
        )
    ){
        Text(
            text = textId,
            modifier = Modifier.padding(vertical = 8.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun PasswordInput(
    passwordState: MutableState<String>,
    labelId: String,
    passwordVisible: MutableState<Boolean>
) {
    val visualTransformation = if (passwordVisible.value)
        VisualTransformation.None
    else PasswordVisualTransformation()

    OutlinedTextField(
        value = passwordState.value,
        onValueChange = {passwordState.value = it},
        label = {
            Text(
                text = labelId,
                color = Color(0xFF2075C6).copy(alpha = 0.8f)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2075C6),
            unfocusedBorderColor = Color(0xFF2075C6).copy(alpha = 0.5f),
            focusedLabelColor = Color(0xFF2075C6),
            cursorColor = Color(0xFF2075C6)
        ),
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            if(passwordState.value.isNotBlank()){
                PasswordVisibleIcon(passwordVisible)
            }
            else null
        }
    )
}

@Composable
fun PasswordVisibleIcon(
    passwordVisible: MutableState<Boolean>
) {
    val image =
        if(passwordVisible.value)
            Icons.Default.VisibilityOff
        else
            Icons.Default.Visibility
    IconButton(onClick = {
        passwordVisible.value = !passwordVisible.value
    }) {
        Icon(
            imageVector = image,
            contentDescription = "",
            tint = Color(0xFF2075C6).copy(alpha = 0.7f)
        )
    }
}

@Composable
fun EmailInput(
    emailState: MutableState<String>,
    labelId: String = "Email"
) {
    InputField(
        valueState = emailState,
        labelId = labelId,
        keyboardType = KeyboardType.Email,
    )
}

@Composable
fun InputField(
    valueState: MutableState<String>,
    labelId: String,
    isSingleLine: Boolean = true,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = {valueState.value = it},
        label = {
            Text(
                text = labelId,
                color = Color(0xFF2075C6).copy(alpha = 0.8f)
            )
        },
        singleLine = isSingleLine,
        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2075C6),
            unfocusedBorderColor = Color(0xFF2075C6).copy(alpha = 0.5f),
            focusedLabelColor = Color(0xFF2075C6),
            cursorColor = Color(0xFF2075C6)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

// Función para dibujar efectos de agua de fondo (igual que en SplashScreen)
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

    // Burbujas flotantes
    for (i in 0..5) {
        val bubbleX = width * (0.1f + i * 0.15f)
        val bubbleY = height * 0.2f + 100f * sin(dropAnimation * 2 * PI + i * PI / 3) + i * 80f
        val bubbleSize = 8f + i * 3f
        val bubbleAlpha = 0.1f - i * 0.015f

        if (bubbleY < height && bubbleY > 0) {
            drawCircle(
                color = Color(0xFF2075C6).copy(alpha = bubbleAlpha),
                radius = bubbleSize,
                center = Offset(bubbleX, bubbleY.toFloat())
            )
        }
    }
}