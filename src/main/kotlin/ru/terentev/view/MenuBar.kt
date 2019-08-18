package ru.terentev.view

import ru.terentev.Controllers.import
import javafx.application.Platform.exit
import javafx.stage.FileChooser
import ru.terentev.Controllers.DBHelper
import tornadofx.*

class MenuBar : Fragment() {
    override val root = menubar() {
        menu("File") {
            item("Add output.xml") {
                this.setOnAction {
                    val ef = arrayOf(FileChooser.ExtensionFilter("Output robot files (.xml)", "*.xml"))
                    var dir = tornadofx.chooseFile("Single + non/block", ef, FileChooserMode.Single)
                    if (!dir.isEmpty()) {
                        run{
                            import(dir)
                            this.disableWhen(statusBar.running)
                            }
                        }
                    }
                }

                item("Save") {
                }
                item("Quit") {
                    this.setOnAction { exit() }

                }
            }
        }
}

