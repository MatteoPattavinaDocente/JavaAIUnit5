package it.epicode.demo.mappe.repository;

import it.epicode.demo.mappe.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
