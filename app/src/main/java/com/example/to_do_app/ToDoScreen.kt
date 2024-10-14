import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration

import androidx.compose.ui.unit.dp
import com.example.to_do_app.Task


@Composable
fun TodoScreen(dbHelper: TaskDbHelper) {
    val taskList = remember { mutableStateListOf<Task>() }
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) } // Task to be edited
    var taskToDelete by remember { mutableStateOf<Task?>(null) } // Task to be deleted

    // Load tasks from the database when the UI is first composed
    LaunchedEffect(Unit) {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var taskName by remember { mutableStateOf("") }

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
                    Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context,"Task name cannot be empty!", Toast.LENGTH_SHORT).show()
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
                    onTaskUpdate = {
                        dbHelper.updateTask(task)
                        taskList.clear()
                        taskList.addAll(dbHelper.getAllTasks()) // Reload tasks
                    },
                    onTaskDelete = { taskToDelete = it; showDeleteDialog = true }, // Set task to delete and show delete dialog
                    onTaskEdit = { taskToEdit = it; showEditDialog = true } // Set task to edit and show dialog
                )
            }
        }

        if (showEditDialog) {
            EditTaskDialog(
                task = taskToEdit!!,
                onDismiss = { showEditDialog = false },
                onSave = {
                    dbHelper.updateTask(task = it)
                    taskList.clear()
                    taskList.addAll(dbHelper.getAllTasks()) // Reload tasks
                    showEditDialog = false
                }
            )
        }

        if (showDeleteDialog && taskToDelete != null) {
            // Confirmation Dialog for task deletion
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete the task '${taskToDelete!!.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            dbHelper.deleteTask(taskToDelete!!.id)
                            taskList.clear()
                            taskList.addAll(dbHelper.getAllTasks()) // Reload tasks
                            Toast.makeText(context, "Task deleted successfully!", Toast.LENGTH_SHORT).show()
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
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
    onTaskDelete: (Task) -> Unit,
    onTaskEdit: (Task) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Adjust padding here to make it uniform
        verticalAlignment = Alignment.CenterVertically, // Align items vertically to the center
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Checkbox for completing/undoing the task
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { isChecked ->
                task.isCompleted = isChecked
                onTaskUpdate(task)
            }
        )

        Text(
            text = task.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp), // Adjust start padding
            maxLines = 1,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )

        // Edit and Delete buttons in a horizontal row with fixed width
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Edit button
            Button(
                onClick = { onTaskEdit(task) },
                modifier = Modifier
                    .defaultMinSize(minWidth = 70.dp)
            ) {
                Text("Edit")
            }

            // Delete button
            Button(
                onClick = { onTaskDelete(task) },
                modifier = Modifier
                    .defaultMinSize(minWidth = 70.dp) // Set fixed button width
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var editedTaskName by remember { mutableStateOf(task.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column {
                TextField(
                    value = editedTaskName,
                    onValueChange = { editedTaskName = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                task.name = editedTaskName // Update the task with the edited name
                onSave(task) // Save the updated task
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


//@Preview
//@Composable
//fun PreviewTodoScreen() {
//    TodoScreen(dbHelper = TaskDbHelper())  // Assume dbHelper is properly initialized
//}
