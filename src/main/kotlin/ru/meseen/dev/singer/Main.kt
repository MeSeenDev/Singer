import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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

    var progress by remember { mutableStateOf(0.0f) }

    var primaryOUT by remember { mutableStateOf("") }
    var newOut by remember { mutableStateOf("") }


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
                    text = "Эталонный: " + filePrimary.name,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally),
                )
                Surface(modifier = Modifier.height(8.dp)) { }
                Text(
                    text = "Проверяемый: " + fileNew.name,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                )
            }
            Button(onClick = {
                if (filePrimary == defFileField) return@Button
                if (fileNew == defFileField) return@Button

                result = ""
                error = ""
                primaryOUT = ""
                newOut = ""

                GlobalScope.launch {
                    progress = 0.1f
                    val primaryOut = query(runCheck(filePrimary))
                    progress = 0.4f
                    val newFile = query(runCheck(fileNew))
                    progress = 0.8f

                    val isSineEquals = primaryOut == newFile
                        primaryOUT = primaryOut
                        newOut = newFile
                        if (isSineEquals) {
                            result = "Равны ли подписи: $isSineEquals"
                        } else {
                            error = "Проверка не удалась $isSineEquals"
                        }

                    progress = 1.0f
                    progress = 0.0f
                }

            }) {
                Text(text = text, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }

            CircularProgressIndicator(progress = progress, modifier = Modifier.padding(vertical = 8.dp))
            Column() {
                Text(text = result, color = accent, fontSize = 16.sp)
                Text(text = error, color = Color.Red, fontSize = 16.sp)
            }

            compare(primaryOUT, newOut)

        }

    }
}

@Composable
@Preview
fun compare(primary: String, secondary: String) {
    Row(horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxSize().scrollable(
            state = ScrollableState { 0f },orientation = Orientation.Vertical
        )
        ) {
        Text(
            text = primary,
            color = accent,
            fontSize = 12.sp,
        )
        Text(
            text = secondary,
            color = greyMid,
            fontSize = 12.sp,
        )
    }
}

fun getPairs(primary: String, secondary: String): Pair<String, String> {
    val primaryTags = primary.slice()
    val secondaryTags = secondary.slice()

    val setOfAllKeys = primaryTags.keys + secondaryTags.keys
    setOfAllKeys.onEachIndexed { index: Int, key: String ->
        if (primaryTags.containsKey(key) && secondaryTags.containsKey(key)) {
            primaryTags[key]

        }

    }
    return "" to ""
}


private fun String.slice(): Map<String, String> =
    split("\n").associate {
        val field = it.split(":", ignoreCase = true, limit = 1)
        if (field.size > 1) {
            field[0] to field[1]
        } else {
            field[0] to field[0]
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
            Window(onCloseRequest = ::exitApplication, title = "Signature Test") {
                App()
            }
        }


        fun runCheck(file: File): Results {
            val runtime = Runtime.getRuntime()
                .exec("powershell.exe .\\src\\main\\assets\\apksigner  verify --print-certs -v '${file.absolutePath}'")
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


