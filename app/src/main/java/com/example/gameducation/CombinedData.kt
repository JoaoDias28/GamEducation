package com.example.gameducation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class CombinedData(
    val perguntas: List<Pergunta>,
    val conteudos_didaticos: List<ConteudoDidatico>,
    val aluno: Aluno,
    val jogo: Jogo,
    val idForm: Int,
    val idProfessor: Int,
    val ordemAtual:Int,
    val tentativa:Int

)

@Parcelize
data class Pergunta(
    // Define properties here based on your JSON structure
    val idpergunta: Int,
    val nome: String,
    val questao: String,
    val correta: Int,
    val idtipo: Int,
    val ordem: Int,
    val respostas: List<Resposta>,

    var userResponse: String?,        // User's response for text questions
    var userChoices: List<String>?,      // User's selected choices for multiple choice questions
    var userMapping: Map<String, String>?,  // User's correspondence mapping for correspondence questions
    var userIsCorrect: Boolean?
) : Parcelable

@Parcelize
data class ConteudoDidatico(
    // Define properties here based on your JSON structure
    val idConteudoDidatico: Int,
    val titulo: String,
    val textoInformativo: String,
    val conteudo_path: String,
    val ordem: Int,
    val tipo: Int,
    var visto: Boolean?
) :Parcelable


data class Aluno(
    // Define properties here based on your JSON structure
    val idAluno: Int,
    val primeiro_nome: String,
    val ultimo_nome: String

)


data class Jogo(
    // Define properties here based on your JSON structure
    val idJogo: Int,
    val NomeJogo: String,
    val caminho_apk: String,
    val package_name: String
)

@Parcelize
data class Resposta(
    // Define properties here based on your JSON structure
    val idresposta: Int,
    val correta: Int,
    val resposta: String,
    val idpergunta: Int,

    //respostas de correspondencia
    val idCorrespondencia: Int,
    val idPergunta: Int,
    val colunaEsquerda: String,
    val colunaDireita: String
) : Parcelable