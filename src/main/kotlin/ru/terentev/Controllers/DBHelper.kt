package ru.terentev.Controllers

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import ru.terentev.Model.*
import tornadofx.*
import java.sql.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class DBHelper(){

    val progress = SimpleIntegerProperty(0)

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "TestResult.db"
        val TABLE_NAME = "tests"
        val TABLE_KW_NAME = "key_words"
        val COLUMN_ID = "_id"
        val COLUMN_NAME = "testname"
        val KW_NAME = "kwname"
        val COLUMN_SUITE = "suitename"
        val COLUMN_LIBRARY = "library"
        val TABLE_TEST = "tests_duration"
        val TABLE_KW = "kw_duration"
        val COLUMN_RESULT_ID = "_id"
        val COLUMN_TEST_ID = "test_id"
        val COLUMN_KW_ID = "kw_id"
        val COLUMN_TIME = "starttime"
        val COLUMN_DURATION = "duration"
        val COLUMN_RESULT = "testresult"
        val COLUMN_BUILDID = "build_id"
        val TABLE_BUILD = "builds"
        val COLUMN_BUILD_ID = "_id"
        val COLUMN_JENKINS_ID = "jenkins_id"
        val COLUMN_ADD_DATE = "add_date"
        val TABLE_SETTINGS_VARIANCE = "settings_var"
        val COLUMN_ACTIVE = "active"
        val COLUMN_PART = "part"
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
    fun <T>returndb(exec: (Connection) -> T):T?{
        val url = "jdbc:sqlite:$DATABASE_NAME"
        try {
            DriverManager.getConnection(url).use { conn ->
                conn.createStatement().execute("PRAGMA foreign_keys = ON")
               return exec(conn)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    fun createTable() {
        opendb { conn ->
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS $TABLE_NAME (\n" +
                            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "$COLUMN_SUITE TEXT NOT NULL,\n"+
                            "$COLUMN_NAME TEXT NOT NULL);\n")
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS $TABLE_KW_NAME (\n" +
                            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "$COLUMN_LIBRARY TEXT NOT NULL,\n"+
                            "$KW_NAME TEXT NOT NULL);\n")
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
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS $TABLE_KW (\n" +
                    "$COLUMN_RESULT_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "$COLUMN_KW_ID INTEGER NOT NULL,\n" +
                    "$COLUMN_TIME TEXT NOT NULL,\n" +
                    "$COLUMN_DURATION REAL NOT NULL,\n" +
                    "$COLUMN_RESULT TEXT NOT NULL,\n" +
                    "$COLUMN_BUILDID INTEGER NOT NULL,\n" +
                    "FOREIGN KEY ($COLUMN_KW_ID) REFERENCES $TABLE_KW_NAME($COLUMN_ID) ON DELETE CASCADE,\n" +
                    "FOREIGN KEY ($COLUMN_BUILDID) REFERENCES $TABLE_BUILD($COLUMN_BUILD_ID) ON DELETE CASCADE);")
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS $TABLE_SETTINGS_VARIANCE (\n" +
                            "$COLUMN_ACTIVE BOOLEAN,\n" +
                            "$COLUMN_PART DOUBLE);\n")
        }
    }

    fun putListInDB(listTest: ArrayList<Test>,listKeyWord: ArrayList<KeyWord>){
                opendb { conn ->
                    var i = 0
                    conn.createStatement().execute("INSERT INTO $TABLE_BUILD($COLUMN_ADD_DATE) VALUES (CURRENT_TIMESTAMP)")
                    var buildIDqury = conn.createStatement().executeQuery("SELECT $COLUMN_BUILD_ID from $TABLE_BUILD WHERE $COLUMN_BUILD_ID = last_insert_rowid()")
                    var buildID = buildIDqury.getInt("_id")
                     conn.createStatement().execute("BEGIN TRANSACTION;")
                        listTest.parallelStream().forEach {
                            progress.set(i++)
                            var rs = conn.createStatement().executeQuery("SELECT _id from $TABLE_NAME WHERE $COLUMN_NAME='" + it.name + "' and $COLUMN_SUITE='" + it.suiteName + "';")
                            if (!rs.next()) {
                                conn.createStatement().execute("INSERT INTO $TABLE_NAME($COLUMN_NAME, $COLUMN_SUITE) VALUES ('" + it.name + "','" + it.suiteName + "');")
                                rs = conn.createStatement().executeQuery("SELECT _id from $TABLE_NAME WHERE $COLUMN_NAME='" + it.name + "' and $COLUMN_SUITE='" + it.suiteName + "';")
                            }
                            var test_id = rs.getInt("_id")
                            rs.close()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                            conn.createStatement().execute("INSERT INTO tests_duration(test_id, starttime, duration, testresult, build_id) " +
                                    "VALUES (" + test_id + ", strftime('%Y-%m-%d %H:%M:%f','" + formatter.format(it.start) + "'), "
                                    + it.time + ", '" + it.status?.name + "', " + buildID + ");")
                        }
                        conn.createStatement().execute("END TRANSACTION;")
                        conn.createStatement().execute("BEGIN TRANSACTION;")
                        listKeyWord.parallelStream().forEach {
                            progress.set(i++)
                            var rs = conn.createStatement().executeQuery("SELECT _id from $TABLE_KW_NAME WHERE $KW_NAME='" + it.name + "' and $COLUMN_LIBRARY='" + it.library + "';")
                            if (!rs.next()) {
                                conn.createStatement().execute("INSERT INTO $TABLE_KW_NAME($KW_NAME, $COLUMN_LIBRARY) VALUES ('" + it.name + "','" + it.library + "');")
                                rs = conn.createStatement().executeQuery("SELECT _id from $TABLE_KW_NAME WHERE $KW_NAME='" + it.name + "' and $COLUMN_LIBRARY='" + it.library + "';")
                            }
                            var kw_id = rs.getInt("_id")
                            rs.close()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                            conn.createStatement().execute("INSERT INTO kw_duration(kw_id, starttime, duration, testresult, build_id) " +
                                    "VALUES (" + kw_id + ", strftime('%Y-%m-%d %H:%M:%f','" + formatter.format(it.start) + "'), "
                                    + it.time + ", '" + it.status?.name + "', " + buildID + ");")
                        }
                        conn.createStatement().execute("END TRANSACTION;")
                }

        }

    val stackTestsID:Stack<UpdatingTest> = Stack()
    fun putDBintoList(): ObservableList<Row?> {
        var result:List<Row?> = ArrayList<Row?>()
        opendb { conn ->
            var rs = conn.createStatement().executeQuery("SELECT * from tests;")
            while (rs.next()){
                stackTestsID.push(UpdatingTest(rs.getInt("_id"),rs.getString(COLUMN_SUITE),rs.getString("testname")))
            }
            val deferred =  (0..stackTestsID.size-1).map{
                GlobalScope.async {
                    return@async returndb<Row> { conn ->
                        var test = stackTestsID.pop()
                        var qury = conn.createStatement().executeQuery("SELECT * from tests_duration where test_id='" + test.id + "';")
                        var duration = ArrayList<StatusTime>()
                        while (qury.next()) {
                            duration.add(StatusTime(qury.getDouble("duration"), qury.getString("starttime"), Status.valueOf(qury.getString("testresult"))))
                        }
                        Row(test.id, test.suit, test.name , duration.count { it.status == Status.PASS },
                                duration.count { it.status == Status.FAIL }, variance(duration), mean(duration))
                    }
                }
            }
            runBlocking {result=deferred.map { it.await() }.toList() }

        }
        return  result.observable()
    }

    val stackId:Stack<UpdatingKW> = Stack()
    fun putDBintoListKW(): ObservableList<RowKW?> {
        var result:List<RowKW?> = ArrayList<RowKW?>()
        opendb { conn ->
            var rs = conn.createStatement().executeQuery("SELECT * from key_words;")
            while (rs.next()){
                stackId.push(UpdatingKW(rs.getInt("_id"),rs.getString(COLUMN_LIBRARY),rs.getString("kwname")))
            }
            val deferred =  (0..stackId.size-1).map{
                GlobalScope.async {
                    return@async returndb<RowKW> { conn ->
                        var kw = stackId.pop()
                        var qury = conn.createStatement().executeQuery("SELECT * from kw_duration where kw_id='" + kw.id + "';")
                        var duration = ArrayList<StatusTime>()
                        while (qury.next()) {
                            duration.add(StatusTime(qury.getDouble("duration"), qury.getString("starttime"), Status.valueOf(qury.getString("testresult"))))
                        }
                        RowKW(kw.id,kw.library ,kw.name , duration.count { it.status == Status.PASS },
                                duration.count { it.status == Status.FAIL }, variance(duration), mean(duration))
                    }
                }
            }
            runBlocking {result=deferred.map { it.await() }.toList() }

        }
        return  result.observable()
    }

    fun getTimeSeries(id: Int): ArrayList<StatusTime> {
        var duration = ArrayList<StatusTime>()
        opendb { conn ->
            var qury:ResultSet
            var id_name:String = COLUMN_TEST_ID
            if ( selectedType.value.equals(TABLE_TEST))
                id_name = COLUMN_TEST_ID
            if ( selectedType.value.equals(TABLE_KW))
                id_name = COLUMN_KW_ID
            when(selectedStatus.value) {
                Status.PASS.name -> qury = conn.createStatement().executeQuery("SELECT * from ${selectedType.value} where $id_name='" + id + "' " +
                        "and testresult='${Status.PASS.name}' ORDER BY starttime, _id;")
                Status.FAIL.name -> qury = conn.createStatement().executeQuery("SELECT * from ${selectedType.value} where $id_name='" + id + "' " +
                        "and testresult='${Status.FAIL.name}' ORDER BY starttime, _id;")
                else -> qury = conn.createStatement().executeQuery("SELECT * from ${selectedType.value} where $id_name='" + id + "' ORDER BY starttime, _id;")
            }
            while (qury.next()) {
                duration.add(StatusTime(qury.getDouble("duration"),qury.getString("starttime"), Status.valueOf(qury.getString("testresult"))))
            }
        }
        return duration
    }

    fun getAllTimeSeries(id:Int):ArrayList<StatusTime> {
        var duration = ArrayList<StatusTime>()
        opendb { conn ->
            var qury:ResultSet
            qury = conn.createStatement().executeQuery("SELECT * from tests_duration where test_id='" + id + "' ORDER BY starttime, _id;")
            while (qury.next()) {
                duration.add(StatusTime(qury.getDouble("duration"),qury.getString("starttime"), Status.valueOf(qury.getString("testresult"))))
            }
        }
        return duration
    }

    fun getAllTimeSeriesKW(id:Int):ArrayList<StatusTime> {
        var duration = ArrayList<StatusTime>()
        opendb { conn ->
            var qury:ResultSet
            qury = conn.createStatement().executeQuery("SELECT * from kw_duration where kw_id='" + id + "' ORDER BY starttime, _id;")
            while (qury.next()) {
                duration.add(StatusTime(qury.getDouble("duration"),qury.getString("starttime"), Status.valueOf(qury.getString("testresult"))))
            }
        }
        return duration
    }

    fun deletingTest(id:Int){
        opendb { conn ->
            var table:String = TABLE_NAME
            if ( selectedType.value.equals(TABLE_TEST))
                table = TABLE_NAME
            if ( selectedType.value.equals(TABLE_KW))
                table = TABLE_KW_NAME
            conn.createStatement().execute("DELETE FROM  ${table} WHERE _id=$id")
        }
    }

    fun deletingFirstFile(){
        opendb { conn ->
            conn.createStatement().execute("DELETE FROM $TABLE_BUILD WHERE _id = (SELECT MIN(_id) FROM $TABLE_BUILD)")
        }
    }

    fun deletingLastFile(){
        opendb { conn ->
            conn.createStatement().execute("DELETE FROM $TABLE_BUILD WHERE _id = (SELECT MAX(_id) FROM $TABLE_BUILD)")
        }
    }

    fun deleteEmptyTests(){
        opendb { conn ->
            conn.createStatement().execute("DELETE FROM tests WHERE _id IN (SELECT _id FROM (select * from tests " +
                    "LEFT JOIN tests_duration td on tests._id = td.test_id) WHERE test_id IS NULL);")
            conn.createStatement().execute("DELETE FROM key_words WHERE _id IN (SELECT _id FROM (select * from key_words " +
                    "LEFT JOIN kw_duration td on  key_words._id = td.kw_id) WHERE kw_id IS NULL);")
        }
    }

    fun clearDB(){
        opendb { conn ->
            conn.createStatement().execute("BEGIN TRANSACTION;")
            conn.createStatement().execute("DELETE FROM $TABLE_TEST")
            conn.createStatement().execute("DELETE FROM $TABLE_NAME")
            conn.createStatement().execute("DELETE FROM $TABLE_BUILD")
            conn.createStatement().execute("DELETE FROM $TABLE_KW_NAME")
            conn.createStatement().execute("END TRANSACTION;")
        }
    }

    fun getSettings(){
        opendb { conn ->
            var res = conn.createStatement().executeQuery("SELECT * FROM $TABLE_SETTINGS_VARIANCE")
            if (res.next()) {
                varianceAssertSettings.active = res.getBoolean(COLUMN_ACTIVE)
                varianceAssertSettings.part = res.getDouble(COLUMN_PART)
            }
        }
    }
    fun saveSettings(){
        opendb { conn ->
            conn.createStatement().execute("DELETE FROM $TABLE_SETTINGS_VARIANCE")
            conn.createStatement().execute("INSERT INTO $TABLE_SETTINGS_VARIANCE($COLUMN_ACTIVE,$COLUMN_PART)" +
                    " VALUES ('${varianceAssertSettings.active.toInt()}',${varianceAssertSettings.part})")
        }
    }
}

fun mean(list:List<StatusTime>):Double{
    var result = list.sumByDouble { it.time }/list.size
    return result
}

fun variance(list:List<StatusTime>):Double{
    var m = (mean(list))
    var result = abs(list.sumByDouble { it.time*it.time }/list.size - m*m)
    return result
}

fun Boolean.toInt():Int{
    return if (this){
        1
    }else 0
}