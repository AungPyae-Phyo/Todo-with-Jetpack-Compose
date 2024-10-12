
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.ui.Alignment


@Composable
fun TodoScreen(dbHelper: TaskDbHelper) {
    val taskList = remember { mutableStateListOf<Task>() }

    // Load tasks from the database when the UI is first composed
    LaunchedEffect(Unit) {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var taskName by remember { mutableStateOf("") }
        var showDialog by remember { mutableStateOf(false) }
        var dialogMessage by remember { mutableStateOf("") }

        // Task input
        TextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Add task button
        Button(
            onClick = {
                if (taskName.isNotEmpty()) {
                    dbHelper.addTask(taskName)
                    taskList.clear()
                    taskList.addAll(dbHelper.getAllTasks()) // Reload tasks
                    taskName = "" // Clear input after adding
                } else {
                    dialogMessage = "Task name cannot be empty!"
                    showDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Display tasks
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(taskList) { task ->
                TaskRow(
                    task = task,
                    onTaskUpdate = { updatedTask ->
                        dbHelper.updateTask(updatedTask.id, updatedTask.name, updatedTask.isCompleted)
                        taskList.clear()
                        taskList.addAll(dbHelper.getAllTasks()) // Reload tasks
                    },
                    onTaskDelete = {
                        dbHelper.deleteTask(task.id)
                        taskList.clear()
                        taskList.addAll(dbHelper.getAllTasks()) // Reload tasks
                    }
                )
            }

        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Message") },
                text = { Text(text = dialogMessage) },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

    }

}

@Composable
fun TaskRow(
    task: Task,
    onTaskUpdate: (Task) -> Unit,
    onTaskDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(task.name)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                task.isCompleted = !task.isCompleted
                onTaskUpdate(task)
            }) {
                Text(if (task.isCompleted) "Undo" else "Complete")
            }

            Button(onClick = {
                onTaskDelete()
            }) {
                Text("Delete")
            }
        }
    }
}



