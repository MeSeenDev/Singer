import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.meseen.dev.singer.FilePicker.chooseFile
import ru.meseen.dev.singer.Results
import ru.meseen.dev.singer.RuntimeCom.runCheck
import ru.meseen.dev.theme.LightColorPalette
import ru.meseen.dev.theme.accent
import ru.meseen.dev.theme.greyMid
import java.io.File

private val defFileField = File("file path")


@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun Singer() {
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


            pickers(
                primary = { filePrimary = it },
                secondary = { fileNew = it }
            )
            textPathFields(
                primaryFile = filePrimary, secondaryFile = fileNew
            )

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
                        error = "Проверка не удалась"
                    }

                    progress = 1.0f
                    progress = 0.0f
                }

            }, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = text, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }
            LinearProgressIndicator(progress = progress, modifier = Modifier.padding(vertical = 8.dp),color = accent)
            Column() {
                Text(text = result, color = accent, fontSize = 16.sp)
                Text(text = error, color = Color.Red, fontSize = 16.sp)
            }

            compare(primaryOUT, newOut)

        }

    }
}





@Composable
fun pickers(
    primary: (file: File) -> Unit,
    secondary: (file: File) -> Unit,
) {
    val pickPrimary by remember { mutableStateOf("Выберите Эталонный файл (.apk)") }
    val pickSecondary by remember { mutableStateOf("  Выберите новый файл (.apk)  ") }
    Row(modifier = Modifier.padding(top = 32.dp)) {
        Button(onClick = {
            primary.invoke(chooseFile(pickPrimary))
        }) {
            Text(text = pickPrimary)

        }
        Spacer(modifier = Modifier.width(16.dp).height(16.dp))
        Button(onClick = {
            secondary.invoke(chooseFile(pickSecondary))
        }) {
            Text(text = pickSecondary)
        }
    }
}

@Composable
fun textPathFields(primaryFile: File, secondaryFile: File) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row(modifier = Modifier.fillMaxWidth().background(greyMid),
            horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Эталонный: " + primaryFile.name,
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 8.dp),
            )
        }
        Surface(modifier = Modifier.height(8.dp)) {}
        Row(modifier = Modifier.fillMaxWidth().background(greyMid),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(modifier = Modifier.height(8.dp)) {}
            Text(
                text = "Проверяемый: " + secondaryFile.name,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)


            )
        }
    }

}


@Composable
@Preview
fun compare(primary: String, secondary: String) {
    Row(horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()
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
    setOfAllKeys.onEachIndexed { _: Int, key: String ->
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

fun main() = singleWindowApplication(
    title = "Signature Test",
    state = WindowState(width = 1600.dp, height = 768.dp),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {
    Singer()
}





