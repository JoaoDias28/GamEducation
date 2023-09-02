@file:Suppress("PreviewAnnotationInFunctionWithParameters")

package com.example.gameducation

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity()  {
    var orderedItemOrder = -1
    val REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent

        if(intent != null){
              orderedItemOrder =  intent.getIntExtra("order",-1)
            }

        setContent {
            AppGameEducation(orderedItemOrder, this)
        }



    }


    fun isAppInstalledByPackageName(context: Context, packageName: String): Boolean {
        val packageManager: PackageManager = context.packageManager

        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

@Preview
@Composable
fun AppGameEducation(orderedItemOrder: Int, context:Context) {
    var codigoAcesso by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var combinedData by remember { mutableStateOf<CombinedData?>(null) }
    val httpClient = OkHttpClient()
    var orderedItems by remember { mutableStateOf<List<Any>?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Handle the result if needed
    }
    Log.d("orderedItemOrder",""+orderedItemOrder)


    @Composable
    fun LoadingScreen(isLoading: Boolean) {
        LaunchedEffect(isLoading) {
            if (isLoading) {
                // Simulate loading delay (replace with your actual data fetching logic)
                delay(5000) // Simulate loading delay
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {





            if (combinedData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = stringResource(id = R.string.game_description),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.access_code),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                BasicTextField(
                    value = codigoAcesso,
                    onValueChange = { newCodigoAcesso -> codigoAcesso = newCodigoAcesso },
                    textStyle = TextStyle.Default.copy(fontSize = 30.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = MaterialTheme.shapes.small
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    LoadingScreen(true)
                } else {
                    Button(
                        onClick = {
                            isLoading = true

                            CoroutineScope(Dispatchers.Main).launch {
                                combinedData = performOkHttpRequest(httpClient, codigoAcesso)
                                isLoading = false

                            }


                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(text = "Submit")
                    }
                }
            } else {


                combinedData?.let { data ->
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val serializedData = combinedData?.toJson()
                    sharedPreferences.edit().putString("combinedData", serializedData).apply()

                    val orderedItemsPergunta = data.perguntas.sortedBy { it.ordem }
                    val orderedItemsConteudoDidatico = data.conteudos_didaticos.sortedBy { it.ordem }

                    val gamePackageName =data.jogo.package_name  // Replace with fetched package name data.jogo.package_name
                    val gameApkUrl = "http://10.0.2.2:80/framework/programador/getJogo.php?apk=${data.jogo.caminho_apk}" // Replace with actual APK URL

                    val intent = Intent(gamePackageName+".ACTION_OPEN_GAME")
                    startActivity(intent)


                        val gameIntent = context.packageManager.getLaunchIntentForPackage(gamePackageName)

                        if (gameIntent != null ) {
                            // Game app is installed, launch it
                            this@MainActivity.startActivityForResult(intent, REQUEST_CODE)


                    } else {
                        Log.d("debugTag","intent is null will try to download")
                        // Game app is not installed, initiate download and installation
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val request = DownloadManager.Request(Uri.parse(gameApkUrl))
                            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "game.apk")
                        val downloadId = downloadManager.enqueue(request)

                        // Save downloadId to retrieve download status later
                        val sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        sharedPrefs.edit().putLong("downloadId", downloadId).apply()

                        // Launch an activity to show download progress to the user
                        // You'll need to create a UI for showing download progress and install button
                    }

                }



                }
            }
        }


}




@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator() // Display the circular progress indicator
            Spacer(modifier = Modifier.height(16.dp)) // Add space between indicator and text
            Text(text = "Aguarde enquanto preparamos o jogo",
                style = MaterialTheme.typography.bodyLarge)
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
    fun CombinedData.toJson(): String {
        return Gson().toJson(this)
    }

}

