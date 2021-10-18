import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.*
import ru.meseen.dev.singer.verify.Certificate
import ru.meseen.dev.singer.FilePicker.chooseFile
import ru.meseen.dev.singer.FilePicker.defFile
import ru.meseen.dev.singer.Results
import ru.meseen.dev.singer.Verifier
import ru.meseen.dev.theme.LightColorPalette
import ru.meseen.dev.theme.accent
import ru.meseen.dev.theme.greyFull
import ru.meseen.dev.theme.greyMid
import java.io.File

private val defFileField = defFile


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

    var primaryCert by remember { mutableStateOf(Certificate()) }
    var secondaryCert by remember { mutableStateOf(Certificate()) }


    /*Verifier.verify(File("I:\\Загрузки\\QP+Gallery+8.5.11.apk"))*/

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

                result = ""
                error = ""

                GlobalScope.launch {
                    progress = 0.1f

                    if (filePrimary == defFileField) {
                        error = "Укажите Эталонный файл"
                        progress = 0.0f
                        return@launch
                    }
                    if (fileNew == defFileField) {
                        error = "Укажите Проверяемый файл"
                        progress = 0.0f
                        return@launch
                    }
                    val primaryFileOut = Verifier.verify(filePrimary)
                    progress = 0.4f
                    val secondaryFileOut = Verifier.verify(fileNew)
                    progress = 0.8f

                    if (primaryFileOut is Results.Error) {
                        error = primaryFileOut.error.localizedMessage
                        progress = 0.0f
                        return@launch
                    }
                    if (secondaryFileOut is Results.Error) {
                        error = secondaryFileOut.error.localizedMessage
                        progress = 0.0f
                        return@launch
                    }
                    if (primaryFileOut is Results.Success && secondaryFileOut is Results.Success) {

                        val isSineEquals = primaryFileOut.data == secondaryFileOut.data
                        if (isSineEquals) {
                            result = "Подписи Эквивалентны"
                        } else {
                            error = "Подписи отличаются"
                        }
                        primaryCert = primaryFileOut.data
                        secondaryCert = secondaryFileOut.data
                    }
                    progress = 1.0f
                    delay(500)
                    progress = 0.0f
                }

            }, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = text, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }
            if(progress != 0.0f)
            LinearProgressIndicator(progress = progress, modifier = Modifier.padding(vertical = 8.dp), color = accent)

            Column() {
                if(result != "")
                Text(text = result, color = accent, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                if(error != "")
                Text(text = error, color = Color.Red, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
            }

            if(primaryCert != Certificate() && secondaryCert != Certificate())
            fileDataList(PrintableItem(primary = primaryCert, secondary = secondaryCert))

        }

    }
}

@Composable
fun fileDataList(printableItem: PrintableItem) {
    Box(
        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        LazyColumn() {
            item {
                certItem(
                    nameField = "Verified using v1 scheme (JAR signing):",
                    primary = printableItem.primary.isVerifiedUsingV1Scheme.toString(),
                    secondary = printableItem.secondary.isVerifiedUsingV1Scheme.toString()
                )
                certItem(
                    nameField = "Verified using v2 scheme (APK Signature Scheme v2):",
                    primary = printableItem.primary.isVerifiedUsingV2Scheme.toString(),
                    secondary = printableItem.secondary.isVerifiedUsingV2Scheme.toString()
                )
                certItem(
                    nameField = "Verified using v3 scheme (APK Signature Scheme v3):",
                    primary = printableItem.primary.isVerifiedUsingV3Scheme.toString(),
                    secondary = printableItem.secondary.isVerifiedUsingV3Scheme.toString()
                )
                certItem(
                    nameField = "Verified using v4 scheme (APK Signature Scheme v4):",
                    primary = printableItem.primary.isVerifiedUsingV4Scheme.toString(),
                    secondary = printableItem.secondary.isVerifiedUsingV4Scheme.toString()
                )
                certItem(
                    nameField = "Verified for SourceStamp:",
                    primary = printableItem.primary.isSourceStampVerified.toString(),
                    secondary = printableItem.secondary.isSourceStampVerified.toString()
                )
                certItem(
                    nameField = "Number of signers:",
                    primary = printableItem.primary.numberOfSignerCertificates.toString(),
                    secondary = printableItem.secondary.numberOfSignerCertificates.toString()
                )
                printableItem.primary.signers.zip(printableItem.secondary.signers).onEachIndexed{index, (primary, secondary) ->
                    certItem(
                        nameField = "Signer #$index certificate DN:",
                        primary = primary.certificateDN.toString(),
                        secondary = secondary.certificateDN.toString()
                    )
                    certItem(
                        nameField = "Signer #$index certificate SHA-256 digest:",
                        primary = primary.certificateSHA256,
                        secondary = secondary.certificateSHA256
                    )
                    certItem(
                        nameField = "Signer #$index certificate SHA-1 digest:",
                        primary = primary.certificateSHA256,
                        secondary = secondary.certificateSHA256
                    )
                    certItem(
                        nameField = "Signer #$index certificate MD5 digest:",
                        primary = primary.certificateMD5,
                        secondary = secondary.certificateMD5
                    )
                    certItem(
                        nameField = "Signer #$index key algorithm:",
                        primary = primary.algorithm,
                        secondary = secondary.algorithm
                    )
                    certItem(
                        nameField = "Signer #$index key size (bits):",
                        primary = if(primary.keySize < 0) "unknown" else primary.keySize.toString(),
                        secondary = if(secondary.keySize < 0) "unknown" else secondary.keySize.toString(),
                    )
                    certItem(
                        nameField = "Signer #$index public key SHA-256 digest:",
                        primary = primary.publicKeySHA256,
                        secondary = secondary.publicKeySHA256
                    )
                    certItem(
                        nameField = "Signer #$index public key SHA-1 digest:",
                        primary = primary.publicKeySHA1,
                        secondary = secondary.publicKeySHA1
                    )
                    certItem(
                        nameField = "Signer #$index public key MD5 digest:",
                        primary = primary.publicKeyMD5,
                        secondary = secondary.publicKeyMD5
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun certItem(nameField: String, primary: String, secondary: String) {

    val colorBag  = if(primary == secondary) accent else Color.Red

    val primarys = SpanStyle(background = Color.Yellow)

    Row(modifier = Modifier.fillMaxSize().padding(8.dp).background(greyFull), horizontalArrangement = Arrangement.SpaceAround) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = nameField, color = Color.White, fontSize = 16.sp, modifier = Modifier.align(Alignment.End).padding(8.dp))
            Text(text = primary, color = Color.White, fontSize = 16.sp, modifier = Modifier.align(Alignment.End).background(colorBag).padding(8.dp))
        }
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(1.0f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = nameField, color = Color.White, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start).padding(8.dp))
            Text(text = secondary, color = Color.White, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start).background(colorBag).padding(8.dp))
        }
    }
}

data class PrintableItem(
    val primary: Certificate,
    val secondary: Certificate
)


@Composable
fun pickers(
    primary: (file: File) -> Unit,
    secondary: (file: File) -> Unit,
) {
    val pickPrimary by remember { mutableStateOf("Выберите Эталонный файл (.apk)") }
    val pickSecondary by remember { mutableStateOf("  Выберите Проверяемый файл (.apk)  ") }
    Row(modifier = Modifier.padding(top = 32.dp)) {
        Button(onClick = {
            GlobalScope.launch(Dispatchers.Default) {
                primary.invoke(chooseFile(pickPrimary))
            }
        }) {
            Text(text = pickPrimary)

        }
        Spacer(modifier = Modifier.width(16.dp).height(16.dp))
        Button(onClick = {
            GlobalScope.launch(Dispatchers.Default) {
                secondary.invoke(chooseFile(pickSecondary))
            }
        }) {
            Text(text = pickSecondary)
        }
    }
}

@Composable
fun textPathFields(primaryFile: File, secondaryFile: File) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().background(greyMid),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Имя Эталонного файла: " + primaryFile.name,
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 8.dp),
            )
        }
        Surface(modifier = Modifier.height(8.dp)) {}
        Row(
            modifier = Modifier.fillMaxWidth().background(greyMid),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(modifier = Modifier.height(8.dp)) {}
            Text(
                text = "Имя Проверяемого файла: " + secondaryFile.name,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)


            )
        }
    }

}


@Composable
@Preview
fun compare(primary: String, secondary: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().horizontalScroll(
            rememberScrollState()
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


private fun String.slice(): Map<String, String> =
    split("\n").associate {
        val field = it.split(":", ignoreCase = true, limit = 2)
        if (field.size > 1) {
            field[0] to field[1]
        } else {
            field[0] to field[0]
        }
    }

fun main() = singleWindowApplication(
    title = "Signature Test",
    state = WindowState(width = 1600.dp, height = 768.dp),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {
    Singer()
}





