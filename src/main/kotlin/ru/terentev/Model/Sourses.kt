package ru.terentev.Model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.chart.XYChart
import ru.terentev.Model.Settings.VarianceAssertSettings
import tornadofx.*

var rows = SortedFilteredList(FXCollections.observableArrayList<Row>())
var rowsKW = SortedFilteredList(FXCollections.observableArrayList<RowKW>())

var datafortime = FXCollections.observableArrayList<XYChart.Data<Number,Number>>()

var dataforhist = FXCollections.observableArrayList<XYChart.Data<String,Number>>()

val statuses = FXCollections.observableArrayList(Status.All.name,Status.FAIL.name, Status.PASS.name)

val selectedStatus = SimpleStringProperty()

val selectedRow = SimpleIntegerProperty(-1)

val selectedType = SimpleStringProperty()

var filter = SimpleStringProperty()

var varianceAssertSettings= VarianceAssertSettings()