package com.example.gameducation
import androidx.compose.foundation.layout.size

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import java.util.Objects
import kotlin.math.roundToInt
import kotlin.random.Random


@Composable
fun SingleChoiceQuestion(question: Pergunta,dataCallback: DataCallback) {
    val selectedAnswer = remember { mutableStateOf<Int?>(null) } // Store the index of the selected answer
    val isSubmitted = remember { mutableStateOf(false) } // Track whether the user has submitted

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Text(
            text = "Pergunta de Escolha Única",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = question.questao,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        question.respostas.forEachIndexed { index, resposta ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                RadioButton(
                    selected = index == selectedAnswer.value,
                    onClick = {
                        selectedAnswer.value = index
                    },
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
                Text(
                    text = resposta.resposta,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
            }
        }

        // Submit button
        Button(
            onClick = {
                isSubmitted.value = true

                val selectedResposta = selectedAnswer.value?.let { question.respostas[it] }
                val isAnswerCorrect = selectedResposta?.correta == 1
                dataCallback.onDataGenerated(isAnswerCorrect)

                question.userIsCorrect = isAnswerCorrect // Store correctness status in the data class
                question.userResponse = selectedResposta?.resposta // Store selected answer in the data class
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Submit")
        }

        // Display correctness feedback after submission
        if (isSubmitted.value) {
            val isAnswerCorrect = selectedAnswer.value?.let { question.respostas[it].correta } == 1
            Text(
                text = if (isAnswerCorrect) "Resposta Correta!" else "Resposta Incorreta!",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isAnswerCorrect) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TrueFalseQuestion(question: Pergunta, dataCallback: DataCallback) {
    val selectedAnswer = remember { mutableStateOf<Boolean?>(null) }
    val isSubmitted = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Text(
            text = "Pergunta de Verdadeiro ou Falso",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = question.questao,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            RadioButton(
                selected = selectedAnswer.value == true,
                onClick = {
                    selectedAnswer.value = true
                },
                modifier = Modifier
                    .padding(end = 8.dp)
            )
            Text(
                text = "True",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(end = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            RadioButton(
                selected = selectedAnswer.value == false,
                onClick = {
                    selectedAnswer.value = false
                }
            )
            Text(
                text = "False",
                style = MaterialTheme.typography.bodyMedium

            )
        }

        // Submit button
        Button(
            onClick = {
                isSubmitted.value = true
                question.userResponse = selectedAnswer.value?.toString() // Store selected answer as "True" or "False"
                 question.userIsCorrect = selectedAnswer.value == (question.correta == 1) // Check if the user's answer is correct
                var correta = selectedAnswer.value == (question.correta == 1)

                dataCallback.onDataGenerated(correta)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Submit")
        }

        // Display correctness feedback after submission
        if (isSubmitted.value) {
            val isAnswerCorrect = selectedAnswer.value == (question.correta == 1)
            Text(
                text = if (isAnswerCorrect) "Resposta Correta!" else "Resposta Incorreta!",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isAnswerCorrect) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
fun MultipleChoiceQuestion(question: Pergunta,dataCallback: DataCallback) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Text(
            text = "Pergunta de Escolha Múltipla (Uma ou mais respostas corretas)",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = question.questao,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val selectedAnswers = remember { mutableStateListOf<String>() }
        val isSubmitted = remember { mutableStateOf(false) }

        question.respostas.forEach { resposta ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Checkbox(
                    checked = selectedAnswers.contains(resposta.resposta),
                    onCheckedChange = { isChecked ->
                        if (!isSubmitted.value) {
                            if (isChecked) {
                                selectedAnswers.add(resposta.resposta)
                            } else {
                                selectedAnswers.remove(resposta.resposta)
                            }
                        }
                    }

                )
                Text(
                    text = resposta.resposta,
                    style = MaterialTheme.typography.bodyMedium

                )
            }
        }

        // Submit button
        Button(
            onClick = {
                isSubmitted.value = true
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Submit")
        }

        // Display correctness feedback after submission
        if (isSubmitted.value) {
            val correctAnswers = question.respostas.filter { it.correta == 1 }.map { it.resposta }
            val correctSelected = selectedAnswers.count { it in correctAnswers }
            val incorrectSelected = selectedAnswers.size - correctSelected

            val correctnessPercentage = if (selectedAnswers.isNotEmpty()) {
                ((correctSelected - incorrectSelected).toFloat() / selectedAnswers.size) * 100
            } else {
                0f
            }

            if(correctnessPercentage < 50){

                var correta = false
                dataCallback.onDataGenerated(correta)
            } else{
                var correta = true
                dataCallback.onDataGenerated(correta)
            }
            Text(
                text = "Corretas: $correctSelected / Incorrectas: $incorrectSelected / Percentagem: ${correctnessPercentage}%",
                style = MaterialTheme.typography.bodyMedium,
                color = if (correctnessPercentage >= 50f) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
@Composable
fun CorrespondenceQuestion(question: Pergunta, dataCallback: DataCallback) {
    val leftColumnItems = question.respostas.map { it.colunaEsquerda }
    val rightColumnItems = question.respostas.map { it.colunaDireita }

    val lines = remember { mutableStateListOf<Line>() }
    val isSubmitted = remember { mutableStateOf(false) }

    val selectedLeftItem = remember { mutableStateOf<String?>(null) }
    val selectedRightItem = remember { mutableStateOf<String?>(null) }
    val key = rememberUpdatedState(selectedLeftItem.value to selectedRightItem.value)
    val textPositions = remember { mutableMapOf<String, Offset>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Pergunta de Correspondência",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = question.questao,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                for (item in leftColumnItems) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                if (selectedLeftItem.value == null) {
                                    selectedLeftItem.value = item
                                } else {
                                    selectedRightItem.value = item
                                }
                            }
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInWindow()
                                val leftX = position.x + (coordinates.size.width / 2f)
                                val startY = position.y + (coordinates.size.height / 2f)
                                textPositions[item] = Offset(leftX, startY)
                            }
                    ) {
                        Text(
                            text = item,
                            fontSize = 16.sp
                        )
                    }
                }

            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f)
                    .key(selectedLeftItem.value to selectedRightItem.value)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()


                ) {
                    // Check if the selected items have changed
                    if (key.value != (selectedLeftItem.value to selectedRightItem.value)) {
                        // Clear the lines when the selection changes
                        lines.clear()
                    }
                    selectedLeftItem.value?.let { leftItem ->
                        selectedRightItem.value?.let { rightItem ->
                            val leftItemPosition = textPositions[leftItem]
                            val rightItemPosition = textPositions[rightItem]
                            Log.d("lineDebug", "Left Item :${leftItemPosition.toString()}")
                            Log.d("lineDebug", "Right Item :${rightItemPosition.toString()}")
                            leftItemPosition?.let { leftPos ->
                                rightItemPosition?.let { rightPos ->

                                    Log.d("lineDebug", "Left Pos : ${leftPos.x.toString()}")
                                    val leftX = leftPos.x
                                    val rightX = rightPos.x
                                    val leftY = leftPos.y
                                    val rightY = rightPos.y

                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(leftX, leftY),
                                        end = Offset(rightX, rightY),
                                        strokeWidth = 2f
                                    )

                                    val line = Line(
                                        startX = leftX,
                                        startY = leftY,
                                        endX = rightX,
                                        endY = rightY,
                                        leftItem = leftItem,
                                        rightItem = rightItem
                                    )
                                    Log.d("lineDebug", "line : ${line.toString()}")
                                    lines.add(line)

                                    selectedLeftItem.value = null
                                    selectedRightItem.value = null
                                }
                            }
                        }
                    }

                    lines.forEach { line ->
                        drawLine(
                            color = Color.Black,
                            start = Offset(line.startX, line.startY),
                            end = Offset(line.endX, line.endY),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                for (item in rightColumnItems) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                if (selectedRightItem.value == null) {
                                    selectedRightItem.value = item
                                } else {
                                    selectedLeftItem.value = item
                                }
                            }
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInWindow()
                                val rightX = position.x + (coordinates.size.width / 2f)
                                val endY = position.y + (coordinates.size.height / 2f)
                                textPositions[item] = Offset(rightX, endY)
                            }
                    ) {
                        Text(
                            text = item,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                isSubmitted.value = true
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Submit")
        }
    }
}

data class Line(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val leftItem: String,
    val rightItem: String
)
