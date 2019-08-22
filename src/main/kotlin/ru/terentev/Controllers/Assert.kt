package ru.terentev.Controllers

import ru.terentev.Model.StatusTime
import ru.terentev.Model.rows
import ru.terentev.Model.varianceAssertSettings
import ru.terentev.view.statusBar
import ru.terentev.view.updateTable
import tornadofx.*


fun assert(){
    runAsync (statusBar){
        updateMessage("Running Assertions...")
        for ((i,row) in rows.withIndex()) {
            updateProgress(i.toDouble(), rows.size.toDouble())
            var massage = ""
            if (varianceAssertSettings.active) {
                massage=massage.plus(varianceAssertSettings.check(DBHelper().getAllTimeSeries(row.id)))
            }
            if (massage.equals("")) {
                row.assertResult = "pass"
            } else
                row.assertResult = massage.dropLast(2)

        }
    }
}