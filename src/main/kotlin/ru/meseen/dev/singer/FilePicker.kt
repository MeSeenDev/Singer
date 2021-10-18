package ru.meseen.dev.singer

import ru.meseen.dev.singer.file.ApkFilter
import java.io.File
import javax.swing.JFileChooser

/**
 * @author Vyacheslav Doroshenko
 */
object FilePicker {

    val defFile = File("File Name")


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

}