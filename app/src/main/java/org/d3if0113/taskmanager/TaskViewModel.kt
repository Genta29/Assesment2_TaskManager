package org.d3if0113.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.d3if0113.taskmanager.data.datastore.PreferencesManager
import org.d3if0113.taskmanager.data.entity.Task
import org.d3if0113.taskmanager.data.entity.TaskPriority
import org.d3if0113.taskmanager.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val tasks: Flow<List<Task>> = repository.getAllTasks()

    // PERBAIKAN 1: Ubah Flow menjadi StateFlow menggunakan stateIn
    // Ini akan memperbaiki error .collectAsState() di UI Anda
    val isDarkTheme: StateFlow<Boolean> = preferencesManager.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false // Tentukan nilai awal, misal false
        )

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    fun getTaskById(id: Long) {
        viewModelScope.launch {
            _selectedTask.value = repository.getTaskById(id)
        }
    }

    fun insertTask(title: String, description: String, priority: TaskPriority) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority,
                createdAt = Date(),
                updatedAt = Date()
            )
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = Date()))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            // PERBAIKAN 2: Gunakan .value untuk mendapatkan nilai Boolean saat ini
            // Ini akan memperbaiki error "Unresolved reference: !"
            preferencesManager.setDarkTheme(!isDarkTheme.value)
        }
    }
}