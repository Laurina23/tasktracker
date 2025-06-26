package com.example.tasktracker.repository;

import com.example.tasktracker.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByCreatedBy(String createdBy);

}
// This code defines a repository interface for managing Task entities in a MongoDB database.
// This interface extends MongoRepository to provide CRUD operations for Task entities.
/*taskRepository.findAll();
taskRepository.findById("abc123");
taskRepository.save(task);
taskRepository.deleteById("xyz456");
*/

// You're saying:
// “This repository works with Task documents whose IDs are Strings.”
