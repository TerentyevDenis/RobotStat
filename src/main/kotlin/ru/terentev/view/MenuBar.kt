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
                    var dirs = tornadofx.chooseFile("Multi + non/block", ef, FileChooserMode.Multi)
                    if (!dirs.isEmpty()) {
                        run{
                            import(dirs)
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

