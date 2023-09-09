package com.example.gameducation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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


                question.userIsCorrect = isAnswerCorrect // Store correctness status in the data class
                question.userResponse = selectedResposta?.resposta // Store selected answer in the data class

                var percentagem = 0.toFloat()
                if(isAnswerCorrect){
                    percentagem = 100.toFloat()
                }

                dataCallback.onDataGenerated(isAnswerCorrect,percentagem ,question,null)
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
                var percentagem = 0.toFloat()
                if(correta){
                   percentagem = 100.toFloat()
                }

                dataCallback.onDataGenerated(correta,percentagem,question,null)
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
            question.userChoices = selectedAnswers.toList()

            val correctnessPercentage = if (selectedAnswers.isNotEmpty()) {
                ((correctSelected - incorrectSelected).toFloat() / selectedAnswers.size) * 100
            } else {
                0f
            }

            if(correctnessPercentage < 50){

                var correta = false
                question.userIsCorrect = false
                dataCallback.onDataGenerated(correta,correctnessPercentage,question,null)
            } else{
                var correta = true
                question.userIsCorrect = true
                dataCallback.onDataGenerated(correta,correctnessPercentage,question,null)
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
    val leftItemPositions = remember { mutableMapOf<String, Offset>() }
    val rightItemPositions = remember { mutableMapOf<String, Offset>() }



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
            Canvas(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                selectedLeftItem.value?.let { leftItem ->
                    selectedRightItem.value?.let { rightItem ->
                        val leftItemPosition = leftItemPositions[leftItem]
                        val rightItemPosition = rightItemPositions[rightItem]
                        leftItemPosition?.let { leftPos ->
                            rightItemPosition?.let { rightPos ->
                                val leftX = leftPos.x
                                val leftY = leftPos.y
                                val rightX = rightPos.x
                                val rightY = rightPos.y


                                val line = Line(
                                    startX = leftX,
                                    startY = leftY,
                                    endX = rightX,
                                    endY = rightY,
                                    leftItem = leftItem,
                                    rightItem = rightItem
                                )

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
                                selectedLeftItem.value = item
                                lines.forEach{ line ->
                                    if(line.leftItem.trim() == item){
                                        lines.remove(line)
                                    }
                                }
                            }
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInWindow()
                                val leftX = position.x + (coordinates.size.width / 2f)
                                val startY = position.y + (coordinates.size.height)
                                leftItemPositions[item] = Offset(leftX, startY)
                            }
                    ) {
                        Text(
                            text = item,
                            fontSize = 16.sp
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
                                selectedRightItem.value = item
                                lines.forEach{ line ->
                                    if(line.rightItem.trim() == item){
                                        lines.remove(line)
                                    }
                                }
                            }
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInWindow()
                                val rightX = position.x + (coordinates.size.width / 2f)
                                val endY = position.y + (coordinates.size.height)
                                rightItemPositions[item] = Offset(rightX, endY)
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
                // Evaluate the user's responses here
                val userResponses = lines.map { it.leftItem to it.rightItem }
                val correctResponses = question.respostas.map { it.colunaEsquerda to it.colunaDireita }

                // Sort both maps before comparison
                val sortedUserResponses = userResponses.sortedBy { it.first }
                val sortedCorrectResponses = correctResponses.sortedBy { it.first }

                question.userMapping = sortedUserResponses.toMap()
                val isCorrect = sortedUserResponses == sortedCorrectResponses
                question.userIsCorrect = isCorrect

                val totalResponses = sortedUserResponses.size
                val correctMatches = sortedUserResponses.count { it in sortedCorrectResponses }
                val correctPercentage = (correctMatches.toDouble() / totalResponses.toDouble()) * 100



                dataCallback.onDataGenerated(isCorrect,correctPercentage.toFloat(), question,null)
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
