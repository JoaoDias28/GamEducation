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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@Suppress("PreviewAnnotationInFunctionWithParameters")
class SecondActivity : ComponentActivity(),DataCallback {
    var orderedItemOrder = -1
    var loadedCombinedData: CombinedData? = null
    var corretaOuVista: Boolean? = false
    var displayedQuestion: Pergunta? = null
    var displayedContent: ConteudoDidatico? = null
    var loadedMaxQuestions: Int? = null
    var avaliacaoAluno: Int? = null
    var avaliacaoPergunta: Int? = null
    var avaliacaoConteudoDidatico: Int? = null
    var avaliacaoJogo: Int? = null
    var avaliacaoForm: Int? = null
    var question:Pergunta? = null
    var conteudoDidatico:ConteudoDidatico? = null
    var tentativa:Int = 0
    var numQuestions:Int = 0
    var percentagem:Float = 0.toFloat()
    override fun onDataGenerated(value: Boolean,percentagem: Float, question: Pergunta?,conteudoDidatico: ConteudoDidatico?) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val serializedData = sharedPreferences.getString("combinedData", null)
        loadedCombinedData = serializedData?.toCombinedData()

        if(question != null) {
            Log.d("debugTag","questao != null")
            this.question = question
            avaliacaoPergunta = question.idpergunta
        }else if(conteudoDidatico != null){
            Log.d("debugTag","conteudo != null")
            this.conteudoDidatico = conteudoDidatico
            avaliacaoConteudoDidatico = conteudoDidatico.idConteudoDidatico
        }
        avaliacaoAluno = loadedCombinedData!!.aluno.idAluno
        avaliacaoJogo = loadedCombinedData!!.jogo.idJogo
        avaliacaoForm = loadedCombinedData!!.idForm
        this.percentagem = percentagem



        loadedMaxQuestions = sharedPreferences.getInt("maxQuestions", 0)
        var package_name = loadedCombinedData!!.jogo.package_name.trim()

        Log.d("debugTag","ordered _"+orderedItemOrder.toString())
        Log.d("debugTag","maxQuestions _"+numQuestions.toString())


        if(avaliacaoAluno != null && avaliacaoJogo != null && avaliacaoForm != null ){
            Log.d("debugTag","sendDataToPHP()")
            sendDataToPHP()
        }

        val intent = Intent(package_name + ".ACTION_RESUME_GAME")
        intent.putExtra("corretaOuVista", value)
        intent.putExtra("orderedItemOrder", orderedItemOrder)
        intent.putExtra("tentativa",tentativa)

        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val serializedData = sharedPreferences.getString("combinedData", null)
        loadedCombinedData = serializedData?.toCombinedData()
        loadedMaxQuestions = sharedPreferences.getInt("maxQuestions", 0)
        numQuestions = loadedCombinedData!!.perguntas.size + loadedCombinedData!!.conteudos_didaticos.size
        tentativa = loadedCombinedData!!.tentativa
        val intent = intent

        if (intent != null) {
            orderedItemOrder = intent.getIntExtra("order", 0)
            tentativa = intent.getIntExtra("tentativa",0)
        }
        if (orderedItemOrder > numQuestions) {
            orderedItemOrder = 1
            tentativa++
        }
        println("numQuestions onCreate: " + numQuestions)
        println("orderedItemOrder onCreate: " + orderedItemOrder)
        println("tentativa onCreate: " + tentativa)
        setContent {

            if ( orderedItemOrder != -1 ) {
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
                val question =
                    if (questionIndex < totalQuestions) loadedCombinedData!!.perguntas[questionIndex] else null
                val content =
                    if (contentIndex < totalContents) loadedCombinedData!!.conteudos_didaticos[contentIndex] else null

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
        Log.d("Second", "chegou ao display question : question : " + question.ordem)
        when (question.idtipo) {
            1 -> SingleChoiceQuestion(question, this)
            2 -> TrueFalseQuestion(question, this)
            3 -> MultipleChoiceQuestion(question, this)
            4 -> CorrespondenceQuestion(question, this)
            // Add more cases if needed
        }
    }

    @Composable
    fun displayContent(content: ConteudoDidatico, corretaOuVista: Boolean) {
        Log.d("Second", "chegou ao display content : ordem :" + content.ordem)
        when (content.tipo) {

            1 -> ImageWidget(content, this)
            2 -> VideoWidget(content, this)
            3 -> TextWidget(content, this)


        }
    }

    fun sendDataToPHP() {
        val client = OkHttpClient()
        val jsonData = JSONObject()
        if(avaliacaoPergunta != null) {
            Log.d("debugTag","sendDataToPHP avaliacaoPergunta != null")
            jsonData.put("avaliacaoPergunta", avaliacaoPergunta)
            jsonData.put("resposta_do_aluno", question?.userResponse.toString())
            jsonData.put("escolhas_aluno", question?.userChoices.toString())
            jsonData.put("correspondencia_aluno", question?.userMapping.toString())
            jsonData.put("correto", question?.userIsCorrect)
        }else if (avaliacaoConteudoDidatico != null){
            Log.d("debugTag","sendDataToPHP avaliacaoConteudoDidatico != null")
            jsonData.put("avaliacaoConteudoDidatico", avaliacaoConteudoDidatico)
            jsonData.put("correto",conteudoDidatico?.visto)
        }

        jsonData.put("avaliacaoAluno", avaliacaoAluno)
        jsonData.put("avaliacaoForm", avaliacaoForm)
        jsonData.put("avaliacaoJogo", avaliacaoJogo)
        jsonData.put("percentagem",percentagem)
        jsonData.put("tentativa",tentativa)



        val requestBody = jsonData.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2:80/framework/putAvaliacao.php") // Replace with your PHP script URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    // Process the response data
                    println("Response from PHP: $responseBody")
                } else {
                    println("HTTP Request Failed : $responseBody")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                println("Request Failed")
            }
        })
    }
}
interface DataCallback {
    fun onDataGenerated(value: Boolean,percentagem: Float, question: Pergunta?, conteudoDidatico: ConteudoDidatico?)

}
