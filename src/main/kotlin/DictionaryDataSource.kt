package org.example

import java.io.File
import java.sql.DriverManager
import java.sql.Statement

fun main() {

    DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
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
        statement.executeUpdate(
            """
                CREATE TABLE IF NOT EXISTS 'users' (
                  'id' integer PRIMARY KEY,
                  'username' varchar,
                  'created_at' timestamp,
                  'chat_id' integer
                );
            """.trimIndent()
        )

        statement.executeUpdate(
            """
                     CREATE TABLE IF NOT EXISTS  'user_answers' (
                    'user_id' integer,
                    'word_id' integer,
                    'correct_answer_count' integer,
                    'updated_at' timestamp,
                    FOREIGN KEY ('word_id') REFERENCES 'words' ('id'),
                    FOREIGN KEY ('user_id') REFERENCES 'users' ('id')
                );
            """.trimIndent()
        )
        updateDictionary(File("words.txt"), statement)
    }
}

fun updateDictionary(file: File, statement: Statement) {
    val lines = file.readLines()

    lines.forEach {
        val wordData = it.split("|")
        statement.executeUpdate("insert into words (text, translate) values ('${wordData[0]}', '${wordData[1]}')")
    }

}


