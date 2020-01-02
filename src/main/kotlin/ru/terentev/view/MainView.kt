package ru.terentev.view

import ru.terentev.app.Styles
import javafx.application.Platform.exit
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.TabPane
import javafx.scene.input.MouseEvent.MOUSE_PRESSED
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ru.terentev.Controllers.DBHelper
import ru.terentev.Model.rows
import ru.terentev.Model.selectedRow
import tornadofx.*
import java.time.LocalDate
import java.time.Period
import javax.swing.text.TableView


class MainView : View("Robot statistic") {

    override val root = vbox{
        prefHeight = 700.0
        prefWidth = 800.0
        add(MenuBar::class)
        splitpane{
            add(TableTests::class)
            add (Charts::class)
            vgrow = Priority.ALWAYS
            orientation=Orientation.VERTICAL
        }
        hbox {
            hbox(4.0) {
                progressbar(statusBar.progress)
                label(statusBar.message)
                visibleWhen(statusBar.running)
                paddingAll = 4
            }
            hgrow = Priority.ALWAYS
        }
        primaryStage.setOnCloseRequest { DBHelper().saveSettings() }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
            DBHelper().createTable()
            DBHelper().getSettings()
        runAsync(statusBar) {
            updateMessage("Updating table...")
            updateProgress(0.4, 1.0)
            updateTable()
            ru.terentev.Controllers.assert()
        }
    }
}

var statusBar = TaskStatus()

