package ru.terentev.view

import javafx.scene.chart.*
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import ru.terentev.Controllers.makeDataForTimeChart
import ru.terentev.Model.*
import tornadofx.*

class Charts: Fragment() {
    override val root = tabpane {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tab("Duration") {
            vbox {
                linechart("Tests' duration", NumberAxis(), NumberAxis()) {
                    series("duration of tests", datafortime)
                    animated = false
                    vgrow = Priority.ALWAYS
                }
                hbox{
                    label ("Use test status ")
                    combobox(selectedStatus, statuses) { getSelectionModel().selectFirst();
                    setOnAction{ makeDataForTimeChart() }
                    }
                    paddingAll=5
                }
            }
        }
        tab("Histogram") {
            vbox {
                barchart("Histogram of tests' duration", CategoryAxis(), NumberAxis()) {
                    series("representation of the distribution of tests' duration", dataforhist)
                    animated = false
                    vgrow = Priority.ALWAYS
                }
                hbox{
                    label ("Use test status ")
                    combobox(selectedStatus, statuses) { getSelectionModel().selectFirst();
                        setOnAction{ makeDataForTimeChart() }
                    }
                    paddingAll=5
                }
            }
        }
    }
}


