// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    val text by remember { mutableStateOf("run") }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    MaterialTheme() {
        Column(
            verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = {
                GlobalScope.launch(Dispatchers.IO) {
                    val runtime = Runtime.getRuntime()
                        .exec("powershell.exe .\\src\\main\\assets\\apksigner  verify --print-certs -v .\\src\\main\\assets\\new\\root.apk")
                    BufferedWriter(OutputStreamWriter(runtime.outputStream)).close()
                    BufferedReader(InputStreamReader(runtime.inputStream)).useLines { lines ->
                        result = lines.toList().joinToString(separator = System.lineSeparator())
                    }
                    BufferedReader(InputStreamReader(runtime.errorStream)).useLines { errors ->
                        error = errors.toList().joinToString(separator = System.lineSeparator())
                    }
                }
            }) {
                Text(text)
            }

            Text(text = result, color = Color.Green)
            Text(text = error, color = Color.Red)

        }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication,title = "Singer") {
        App()
    }
}
