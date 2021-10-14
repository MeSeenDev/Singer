package ru.meseen.dev.singer.file

import java.io.File
import javax.swing.filechooser.FileFilter

/**
 * @author Vyacheslav Doroshenko
 */
class ApkFilter  : FileFilter() {
    override fun accept(f: File?): Boolean {
        val isDirectory = f?.isDirectory ?: false
        val isApk = f?.name?.endsWith(".apk") ?: false
        if (isDirectory) return true
        if (isApk) return true

        return false
    }

    override fun getDescription(): String = ".apk"

}