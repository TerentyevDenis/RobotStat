package ru.terentev.Model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.chart.XYChart
import tornadofx.*
import java.util.*

var rows = SortedFilteredList(FXCollections.observableArrayList<Row>())

var datafortime = FXCollections.observableArrayList<XYChart.Data<Number,Number>>()

var dataforhist = FXCollections.observableArrayList<XYChart.Data<String,Number>>()

val statuses = FXCollections.observableArrayList(Status.All.name,Status.FAIL.name, Status.PASS.name)

val selectedStatus = SimpleStringProperty()

val selectedRow = SimpleIntegerProperty()

var filter = SimpleStringProperty()