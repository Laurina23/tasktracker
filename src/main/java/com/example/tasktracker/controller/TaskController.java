package com.example.tasktracker.controller;

import com.example.tasktracker.model.Task;
import com.example.tasktracker.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public List<Task> getAllTasks(Authentication authentication) {
        return taskRepository.findByCreatedBy(authentication.getName());
    }


    @PostMapping
    public Task createTask(@RequestBody Task task, Authentication authentication) {
        task.setCreatedBy(authentication.getName());
        return taskRepository.save(task);
    }

    @GetMapping("/{id}")
    public Optional<Task> getTaskById(@PathVariable String id) {
        return taskRepository.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id, @RequestBody Task updatedTask, Authentication authentication) {
        return taskRepository.findById(id).map(task -> {
            if (!task.getCreatedBy().equals(authentication.getName())) {
                return ResponseEntity.status(403).body("You are not allowed to update this task");
            }

            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setCompleted(updatedTask.isCompleted());
            return ResponseEntity.ok(taskRepository.save(task));
        }).orElse(ResponseEntity.status(404).body("Task not found"));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id, Authentication authentication) {
        return taskRepository.findById(id).map(task -> {
            if (!task.getCreatedBy().equals(authentication.getName())) {
                return ResponseEntity.status(403).body("You are not allowed to delete this task");
            }

            taskRepository.deleteById(id);
            return ResponseEntity.ok("Task deleted successfully");
        }).orElse(ResponseEntity.status(404).body("Task not found"));
    }

}
