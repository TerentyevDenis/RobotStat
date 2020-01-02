package ru.terentev.view
import javafx.beans.property.StringProperty
import  ru.terentev.Model.*
import javafx.collections.FXCollections
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import ru.terentev.Controllers.*
import tornadofx.*
import tornadofx.Stylesheet.Companion.tab
import java.util.regex.PatternSyntaxException

class TableTests : Fragment() {
    override val root = vbox {
        textfield {
            promptText = "Search (regexp allowed)"
            val validator = ValidationContext()
            validator.addValidator(this, this.textProperty()) {
                val t = text.isRegexp()
                if (t != null) {
                    error(text.isRegexp())
                } else null
            }
            rows.filterWhen(textProperty()) { qury, item ->
                if (validator.isValid) {
                    if (qury.isEmpty()) {
                        true
                    } else {
                        item.name.matches(qury.toRegex())
                    }
                } else true
            }

        }
        tabpane {
            tab("tests") {
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                tableview(rows) {
                    column("suite", Row::suite).pctWidth(19.0)
                    column("name", Row::name).pctWidth(19.0)
                    column("pass amount", Row::pass).pctWidth(8.0)
                    column("fail amount", Row::fail).pctWidth(8.0)
                    column("time variance", Row::varianceform).pctWidth(8.0)
                    column("mean time", Row::meanform).pctWidth(8.0)
                    column("assert massages", Row::assertResult).pctWidth(30.0)
                    columnResizePolicy = SmartResize.POLICY
                    vgrow = Priority.ALWAYS
                    onUserSelect(1) { row ->
                        selectedRow.set(row.id)
                        selectedType.set("tests_duration")
                        makeDataForTimeChart()
                    }
                }
            }
            tab("keywords") {
                tableview(rowsKW) {
                    column("library", RowKW::library).pctWidth(19.0)
                    column("name", RowKW::name).pctWidth(19.0)
                    column("pass amount", RowKW::pass).pctWidth(8.0)
                    column("fail amount", RowKW::fail).pctWidth(8.0)
                    column("time variance", RowKW::varianceform).pctWidth(8.0)
                    column("mean time", RowKW::meanform).pctWidth(8.0)
                    column("assert massages", RowKW::assertResult).pctWidth(30.0)
                    columnResizePolicy = SmartResize.POLICY
                    vgrow = Priority.ALWAYS
                    onUserSelect(1) { row ->
                        selectedRow.set(row.id)
                        selectedType.set("kw_duration")
                        makeDataForTimeChart()
                    }
                }
            }
        }
    }

}

fun updateTable(){
    if (rows.isNotEmpty()) {
        rows.clear()
    }
        rows.addAll(DBHelper().putDBintoList())
    if (rowsKW.isNotEmpty()) {
        rowsKW.clear()
    }
        rowsKW.addAll(DBHelper().putDBintoListKW())
}


