import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.meseen.dev.singer.Results
import ru.meseen.dev.singer.file.ApkFilter
import ru.meseen.dev.theme.LightColorPalette
import ru.meseen.dev.theme.accent
import ru.meseen.dev.theme.greyMid
import java.io.*
import javax.swing.JFileChooser

private val defFileField = File("file path")

@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    val text by remember { mutableStateOf("Запустить") }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    var filePrimary by remember { mutableStateOf(defFileField) }
    var fileNew by remember { mutableStateOf(defFileField) }


    MaterialTheme(colors = LightColorPalette) {
        Column(
            verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {

            Row(modifier = Modifier.padding(top = 32.dp)) {
                Button(onClick = {
                    filePrimary = chooseFile("Выберите Эталонный файл")
                }) {
                    Text(text = "Выберите Эталонный файл")

                }
                Spacer(modifier = Modifier.width(16.dp).height(16.dp))
                Button(onClick = {
                    fileNew = chooseFile("Выберите Проверяемый файл")
                }) {
                    Text(text = "Выберите Проверяемый файл")

                }
            }
            Column(modifier = Modifier.background(greyMid).fillMaxWidth()) {
                Text(
                    text = filePrimary.name,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally),
                )
                Text(text = fileNew.name,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally))
            }

            Button(onClick = {
                if (filePrimary == defFileField) return@Button
                if (fileNew == defFileField) return@Button
                GlobalScope.launch {
                    val primaryOut = query(runCheck(filePrimary))
                    val newFile = query(runCheck(fileNew))
                    val isSineEquals = primaryOut == newFile
                    if(isSineEquals){
                        result = isSineEquals.toString()
                    }else{
                        error = isSineEquals.toString()
                    }
                    println("$primaryOut  \n  $newFile")
                }

            }) {
                Text(text = text, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }


            Column() {
                Text(text = result, color = accent,fontSize = 16.sp)
                Text(text = error, color = Color.Red,fontSize = 16.sp)
            }


        }

    }
}

fun query(results: Results): String =
    when (results) {
        is Results.Success -> {
            results.data
        }
        is Results.Error -> {
            results.error
        }
        else -> {
            "Fail"
        }
    }

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Singer") {
        App()
    }
}


fun runCheck(file: File): Results {
    val runtime = Runtime.getRuntime()
        .exec("powershell.exe .\\src\\main\\assets\\apksigner  verify --print-certs -v ${file.absolutePath}")
    BufferedWriter(OutputStreamWriter(runtime.outputStream)).close()
    BufferedReader(InputStreamReader(runtime.inputStream)).useLines { lines ->
        val result = lines.toList().joinToString(separator = System.lineSeparator())
        if (result.isNotBlank())
            return Results.Success(result)
    }

    BufferedReader(InputStreamReader(runtime.errorStream)).useLines { errors ->
        val error = errors.toList().joinToString(separator = System.lineSeparator())
        if (error.isNotBlank())
            return Results.Error(error)
    }
    return Results.Fail
}

private val defFile = File("file path")

fun chooseFile(name: String = "Выберите Файл"): File {
    JFileChooser().run {
        currentDirectory = File(".")
        dialogTitle = name
        fileFilter = ApkFilter()
        fileSelectionMode = JFileChooser.FILES_ONLY
        isAcceptAllFileFilterUsed = false
        return if (showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedFile
        } else {
            defFile
        }

    }
}


