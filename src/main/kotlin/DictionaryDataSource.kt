package org.example

import java.io.File
import java.sql.DriverManager

fun main() {


    updateDictionary(File("words.txt"))
}

fun updateDictionary(file: File) {

    val lines = file.readLines()

    DriverManager.getConnection("jdbc:sqlite:data.db")
        .use { connection ->
            val statement = connection.createStatement()
            statement.executeUpdate(
                """
                        CREATE TABLE IF NOT EXISTS 'words' (
                    'id' integer PRIMARY KEY AUTOINCREMENT,
                    'text' varchar,
                    'translate' varchar
                );
            """.trimIndent()
            )

            lines.forEach { it ->
                val wordData = it.split("|")
                statement.executeUpdate("insert into words (text, translate) values ('${wordData[0]}', '${wordData[1]}')")
            }


        }

}
