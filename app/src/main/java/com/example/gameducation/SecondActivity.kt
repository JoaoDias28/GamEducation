package com.example.gameducation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Suppress("PreviewAnnotationInFunctionWithParameters")
class SecondActivity : ComponentActivity(),DataCallback  {
        var orderedItemOrder = -1
        var loadedCombinedData: CombinedData? = null
        var corretaOuVista: Boolean? = false
    var displayedQuestion: Pergunta? = null
    var displayedContent: ConteudoDidatico? = null

    override fun onDataGenerated(value: Boolean) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val serializedData = sharedPreferences.getString("combinedData", null)
        loadedCombinedData = serializedData?.toCombinedData()
        var package_name = loadedCombinedData!!.jogo.package_name.trim()

        val intent = Intent(package_name + ".ACTION_RESUME_GAME")
        intent.putExtra("corretaOuVista", value)
        intent.putExtra("orderedItemOrder",orderedItemOrder)

        startActivity(intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val serializedData = sharedPreferences.getString("combinedData", null)
        loadedCombinedData = serializedData?.toCombinedData()

        val intent = intent

        if(intent != null){
            orderedItemOrder =  intent.getIntExtra("order",-1)
        }
        println("orderedItemOrder onCreate: "+orderedItemOrder)

            setContent {
                if (orderedItemOrder != -1) {
                    previewQuestionOrContent()
                }
            }
        }




    suspend fun performOkHttpRequest(client: OkHttpClient, codigoAcesso: String): CombinedData {
        val url = "http://10.0.2.2:80/framework/GetCodigoAcesso.php?codigoAcesso=$codigoAcesso"
        val request = Request.Builder()
            .url(url)
            .build()


        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                println("Response Body: $responseBody") // Print the response body for debugging

                val gson = Gson()
                gson.fromJson(responseBody, CombinedData::class.java)
            } catch (e: Exception) {
                e.printStackTrace() // Print the exception for debugging
                // Handle the exception or rethrow it as needed
                throw e
            }
        }

    }
    fun String.toCombinedData(): CombinedData {
        return Gson().fromJson(this, CombinedData::class.java)
    }

    @Composable
    fun previewQuestionOrContent() {
        if (loadedCombinedData != null) {
            val totalQuestions = loadedCombinedData!!.perguntas.size
            val totalContents = loadedCombinedData!!.conteudos_didaticos.size

            var questionIndex = 0
            var contentIndex = 0

            while (questionIndex < totalQuestions || contentIndex < totalContents) {
                val question = if (questionIndex < totalQuestions) loadedCombinedData!!.perguntas[questionIndex] else null
                val content = if (contentIndex < totalContents) loadedCombinedData!!.conteudos_didaticos[contentIndex] else null

                if (question != null && orderedItemOrder == question.ordem) {
                    displayedQuestion = question
                    displayedContent = null
                    break
                } else if (content != null && orderedItemOrder == content.ordem) {
                    displayedContent = content
                    displayedQuestion = null
                    break
                }

                questionIndex++
                contentIndex++
            }
        }

        if (displayedQuestion != null) {
            displayQuestion(displayedQuestion!!, corretaOuVista!!)
        } else if (displayedContent != null) {
            displayContent(displayedContent!!, corretaOuVista!!)
        }
    }
    @Composable
    fun displayQuestion(question: Pergunta, corretaOuVista: Boolean) {
        Log.d("Second","chegou ao display question : question : "+question.ordem)
        when (question.idtipo) {
            1 -> SingleChoiceQuestion(question,this)
            2 -> TrueFalseQuestion(question,this)
            3 -> MultipleChoiceQuestion(question,this)
            4 -> CorrespondenceQuestion(question,this)
            // Add more cases if needed
        }
    }

    @Composable
    fun displayContent(content: ConteudoDidatico, corretaOuVista: Boolean) {
        Log.d("Second","chegou ao display content : ordem :"+content.ordem)
        when (content.tipo) {
            // Handle different content types based on the 'tipo' property
            1 -> ImageWidget(content,this)
            2 -> TextWidget(content,this)
            3 -> VideoWidget(content,this)
            // Add more cases if needed
        }
    }
}
interface DataCallback {
    fun onDataGenerated(value: Boolean) // You can replace 'Any' with the actual data type

}
