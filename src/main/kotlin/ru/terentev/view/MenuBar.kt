package ru.terentev.view

import ru.terentev.Controllers.import
import javafx.application.Platform.exit
import javafx.stage.FileChooser
import ru.terentev.Controllers.DBHelper
import ru.terentev.Controllers.export
import ru.terentev.Model.rows
import ru.terentev.Model.rowsKW
import ru.terentev.Model.selectedRow
import ru.terentev.Model.selectedType
import tornadofx.*
import javax.xml.soap.Node

class MenuBar : Fragment() {
    override val root = menubar() {
        menu("File") {
            item("Add output.xml") {
                this.setOnAction {
                    val ef = arrayOf(FileChooser.ExtensionFilter("Output robot files (.xml)", "*.xml"))
                    var dirs = tornadofx.chooseFile("Multi + non/block", ef, FileChooserMode.Multi)
                    if (!dirs.isEmpty()) {
                        run {
                            import(dirs)
                            this.disableWhen(statusBar.running)
                        }
                    }
                }
            }
            item("Save test data") {
                this.setOnAction {
                    val ef = arrayOf(FileChooser.ExtensionFilter("Files with data (.dat)", "*.dat"))
                    var dir = tornadofx.chooseFile("Choose file", ef, FileChooserMode.Save)
                    if (!dir.isEmpty()) {
                        run {
                            export(dir)
                            this.disableWhen(statusBar.running)
                        }
                    }
                }
            }
            item("Assert settings"){
                setOnAction{ openInternalWindow<AutoCheckSettings>( modal = false,owner = find(MainView::class).root) }
            }
            item("Quit") {
                this.setOnAction { exit() }

            }
        }
        menu("Deleting") {
            item("Delete selected row") {
                this.setOnAction {
                    if (selectedRow.value != -1) {
                        confirm("Confirm delete", "Do you want to delete statistic of ${getName(selectedRow.value)} test?") {
                            runAsync(statusBar) {
                                DBHelper().deletingTest(selectedRow.value)
                                updateMessage("Updating table...")
                                updateProgress(0.4, 1.0)
                                updateTable()
                            }
                        }
                    }
                }
            }
            item("Delete last uploaded file") {
                this.setOnAction {
                    confirm("Confirm delete", "Do you want to delete statistic of last file?") {
                        runAsync(statusBar) {
                            DBHelper().deletingLastFile()
                            DBHelper().deleteEmptyTests()
                            updateMessage("Updating table...")
                            updateProgress(0.4, 1.0)
                            updateTable()
                        }
                    }
                }
            }
            item("Delete first uploaded file") {
                this.setOnAction {
                    confirm("Confirm delete", "Do you want to delete statistic of first file?") {
                        runAsync(statusBar) {
                            DBHelper().deletingFirstFile()
                            DBHelper().deleteEmptyTests()
                            updateMessage("Updating table...")
                            updateProgress(0.4, 1.0)
                            updateTable()
                        }
                    }
                }
            }
            item("Clear database") {
                this.setOnAction {
                    confirm("Confirm delete", "Do you want to clear database?") {
                        runAsync(statusBar) {
                            DBHelper().clearDB()
                            updateMessage("Updating table...")
                            updateProgress(0.4, 1.0)
                            updateTable()
                        }
                    }
                }
            }
        }
    }
}

fun getName(id: Int): String {
    if (selectedType.value.equals("kw_duration"))
        return rowsKW.filter { it.id == id }.last().name
    return rows.filter { it.id == id }.last().name
}

