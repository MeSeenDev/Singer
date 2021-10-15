package ru.meseen.dev.singer

import java.io.*

/**
 * @author Vyacheslav Doroshenko
 */
object RuntimeCom {
    fun execute(command : String){

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

}