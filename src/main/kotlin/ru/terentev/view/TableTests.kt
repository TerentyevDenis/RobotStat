package ru.terentev.view
import javafx.beans.property.StringProperty
import  ru.terentev.Model.*
import javafx.collections.FXCollections
import javafx.scene.layout.Priority
import ru.terentev.Controllers.DBHelper
import ru.terentev.Controllers.isRegexp
import ru.terentev.Controllers.makeDataForTimeChart
import tornadofx.*
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

        tableview(rows) {
            column("suite", Row::suite).pctWidth(19.0)
            column("name", Row::name).pctWidth(19.0)
            column("pass amount", Row::pass).pctWidth(8.0)
            column("fail amount", Row::fail).pctWidth(8.0)
            column("time variance", Row::varianceform).pctWidth(8.0)
            column("mean time", Row::meanform).pctWidth(8.0)
            column("assert massages",Row::assertResult).pctWidth(30.0)
            columnResizePolicy = SmartResize.POLICY
            vgrow=Priority.ALWAYS
            onUserSelect(1) { row ->
                selectedRow.set(row.id)
                makeDataForTimeChart()
            }
        }
    }

}

fun updateTable(){
    if (rows.isNotEmpty()) {
        rows.clear()
    }
        rows.addAll(DBHelper().putDBintoList())
}


