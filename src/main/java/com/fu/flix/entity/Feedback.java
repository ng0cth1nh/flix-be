package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Getter
@Setter
@Table(name = "feedbacks")
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long handleByAdminId;

    private Long createdById;

    private Long userId;

    private String title;

    private String description;

    private String statusId;

    private String type;

    private String requestCode;

    private String response;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "feedback_images",
            joinColumns = @JoinColumn(name = "feedback_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id"))
    private Collection<Image> images = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
