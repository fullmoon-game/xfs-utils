package net.fullmoon.util

import java.io.File
import java.io.FileInputStream
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.system.exitProcess

class XfsUnpacker {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val input = promptInput()
            val output = promptOutput()

            runCatching {
                FileInputStream(input).use {
                    XfsParser(it, output)
                    JOptionPane.showMessageDialog(null, "File successfully extracted!")
                }
            }.onFailure {
                it.printStackTrace()
                JOptionPane.showMessageDialog(null, "Failed to decompress file:\n${it.message}\nStack trace has been printed to the console.")
            }
        }

        fun promptOutput(): File {
            val dialog = JFileChooser()
            dialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialog.dialogTitle = "Select output folder"
            dialog.isVisible = true

            val inputFile = dialog.showSaveDialog(null)
            if (inputFile != 0) exitProcess(0)

            return dialog.selectedFile
        }

        fun promptInput(): File {
            val dialog = JFileChooser()
            dialog.fileFilter = FileNameExtensionFilter("Xenesis File System", "xfs")
            dialog.isVisible = true

            val inputFile = dialog.showOpenDialog(null)
            if (inputFile != 0) exitProcess(0)

            return dialog.selectedFile
        }

    }
}