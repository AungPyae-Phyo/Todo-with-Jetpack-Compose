import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Data class for Task
data class Task(
    var id: Int = 0,
    var name: String,
    var isCompleted: Boolean = false
)

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "todolist.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_COMPLETED = "completed"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_COMPLETED INTEGER NOT NULL DEFAULT 0
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Add a task to the database
    fun addTask(name: String) {
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
        }
        writableDatabase.insert(TABLE_NAME, null, values)
    }

    // Get all tasks from the database
    @SuppressLint("Range")
    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val cursor: Cursor = readableDatabase.query(TABLE_NAME, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            val isCompleted = cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETED)) == 1
            tasks.add(Task(id, name, isCompleted))
        }
        cursor.close()
        return tasks
    }

    // Update task completion status
    fun updateTask(id: Int, name: String, isCompleted: Boolean) {
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_COMPLETED, if (isCompleted) 1 else 0)
        }
        writableDatabase.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // Delete a task from the database
    fun deleteTask(id: Int) {
        writableDatabase.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}
