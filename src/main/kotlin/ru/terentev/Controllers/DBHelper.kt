package ru.terentev.Controllers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import ru.terentev.Model.*
import ru.terentev.view.MainView
import ru.terentev.view.statusBar
import ru.terentev.view.updateTable
import sun.invoke.empty.Empty
import tornadofx.*
import java.io.File
import java.sql.*
import java.text.DecimalFormat
import java.util.ArrayList
import java.time.format.DateTimeFormatter
import javax.swing.plaf.BorderUIResource
import kotlin.math.abs


class DBHelper(){

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "TestResult.db"
        val TABLE_NAME = "tests"
        val COLUMN_ID = "_id"
        val COLUMN_NAME = "testname"
        val TABLE_TEST = "tests_duration"
        val COLUMN_RESULT_ID = "_id"
        val COLUMN_TEST_ID = "test_id"
        val COLUMN_TIME = "starttime"
        val COLUMN_DURATION = "duration"
        val COLUMN_RESULT = "testresult"
        val COLUMN_BUILDID = "build_id"
        val TABLE_BUILD = "builds"
        val COLUMN_BUILD_ID = "_id"
        val COLUMN_JENKINS_ID = "jenkins_id"
        val COLUMN_ADD_DATE = "add_date"
    }

    fun opendb(exec:(Connection)->Unit){
        val url = "jdbc:sqlite:$DATABASE_NAME"
        try {
            DriverManager.getConnection(url).use { conn ->
                   conn.createStatement().execute("PRAGMA foreign_keys = ON")
                   exec(conn)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun createTable() {
        opendb { conn ->
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS $TABLE_NAME (\n" +
                            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "$COLUMN_NAME TEXT NOT NULL UNIQUE);\n")
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS $TABLE_BUILD (\n" +
                            "$COLUMN_BUILD_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "$COLUMN_JENKINS_ID INTEGER,\n"+
                            "$COLUMN_ADD_DATE TEXT NOT NULL);\n")

            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS $TABLE_TEST (\n" +
                    "$COLUMN_RESULT_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "$COLUMN_TEST_ID INTEGER NOT NULL,\n" +
                    "$COLUMN_TIME TEXT NOT NULL,\n" +
                    "$COLUMN_DURATION REAL NOT NULL,\n" +
                    "$COLUMN_RESULT TEXT NOT NULL,\n" +
                    "$COLUMN_BUILDID INTEGER NOT NULL,\n" +
                    "FOREIGN KEY ($COLUMN_TEST_ID) REFERENCES $TABLE_NAME($COLUMN_ID) ON DELETE CASCADE,\n" +
                    "FOREIGN KEY ($COLUMN_BUILDID) REFERENCES $TABLE_BUILD($COLUMN_BUILD_ID) ON DELETE CASCADE);")
        }
    }

    fun putListInDB(dirs: List<File>){
        runAsync (statusBar) {
            for (dir in dirs) {
                val list = xmlparser(dir.inputStream())
                updateMessage("Loading ${dir.name}...")
                opendb { conn ->
                    conn.createStatement().execute("INSERT INTO $TABLE_BUILD($COLUMN_ADD_DATE) VALUES (CURRENT_TIMESTAMP)")
                    var buildIDqury = conn.createStatement().executeQuery("SELECT $COLUMN_BUILD_ID from $TABLE_BUILD WHERE $COLUMN_BUILD_ID = last_insert_rowid()")
                    var buildID = buildIDqury.getInt("_id")
                    for ((i, test) in list.withIndex()) {
                        updateProgress(i.toDouble(), list.size.toDouble())
                        if (!conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY).executeQuery(
                                        "SELECT _id from $TABLE_NAME WHERE $COLUMN_NAME='" + test.name + "';").next()) {
                            conn.createStatement().execute("INSERT INTO $TABLE_NAME($COLUMN_NAME) VALUES ('" + test.name + "');")
                        }
                        var rs = conn.createStatement().executeQuery("SELECT _id from $TABLE_NAME WHERE $COLUMN_NAME='" + test.name + "';")
                        rs.next()
                        var test_id = rs.getInt("_id")
                        rs.close()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        conn.createStatement().execute("INSERT INTO tests_duration(test_id, starttime, duration, testresult, build_id) " +
                                "VALUES (" + test_id + ", strftime('%Y-%m-%d %H:%M:%f','" + formatter.format(test.start) + "'), "
                                + test.time + ", '" + test.status?.name + "', " + buildID + ");")
                    }
                }
            }
                updateMessage("Updating table...")
                updateProgress(0.4, 1.0)
                updateTable()

        }
    }

    fun putDBintoList(): ObservableList<Row> {
        var result = ArrayList<Row>()
        opendb { conn ->
            var rs = conn.createStatement().executeQuery("SELECT * from tests;")
            while (rs.next()){
                var id = rs.getInt("_id")
                var qury = conn.createStatement().executeQuery("SELECT * from tests_duration where test_id='"+id+"';")
                var duration = ArrayList<StatusTime>()
                while (qury.next()){
                    duration.add(StatusTime(qury.getDouble("duration"), Status.valueOf(qury.getString("testresult"))))
                }
                var row=Row(id,rs.getString("testname"),duration.count{it.status==Status.PASS},
                        duration.count{it.status==Status.FAIL}, variance(duration), mean(duration))
                result.add(row)
            }
        }
        return  result.observable()
    }

    fun getTimeSeries(id: Int): ArrayList<StatusTime> {
        var duration = ArrayList<StatusTime>()
        opendb { conn ->
            var qury:ResultSet
            when(selectedStatus.value) {
                Status.PASS.name -> qury = conn.createStatement().executeQuery("SELECT * from tests_duration where test_id='" + id + "' " +
                        "and testresult='${Status.PASS.name}' ORDER BY starttime, _id;")
                Status.FAIL.name -> qury = conn.createStatement().executeQuery("SELECT * from tests_duration where test_id='" + id + "' " +
                        "and testresult='${Status.FAIL.name}' ORDER BY starttime, _id;")
                else -> qury = conn.createStatement().executeQuery("SELECT * from tests_duration where test_id='" + id + "' ORDER BY starttime, _id;")
            }
            while (qury.next()) {
                duration.add(StatusTime(qury.getDouble("duration"), Status.valueOf(qury.getString("testresult"))))
            }
        }
        return duration
    }
}

fun mean(list:ArrayList<StatusTime>):Double{
    var result = list.sumByDouble { it.time }/list.size
    return result
}

fun variance(list:ArrayList<StatusTime>):Double{
    var m = (mean(list))
    var result = abs(list.sumByDouble { it.time*it.time }/list.size - m*m)
    return result
}