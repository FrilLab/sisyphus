package com.sisyphus.backend.persistence;

import com.sisyphus.backend.category.entity.Category;
import com.sisyphus.backend.category.repository.CategoryRepository;
import com.sisyphus.backend.note.entity.Note;
import com.sisyphus.backend.note.repository.NoteRepository;
import com.sisyphus.backend.tag.entity.Tag;
import com.sisyphus.backend.tag.repository.TagRepository;
import com.sisyphus.backend.user.entity.User;
import com.sisyphus.backend.user.repository.UserRepository;
import com.sisyphus.backend.user.util.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryPersistenceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void tagUniquenessIsScopedToItsOwner() {
        User first = userRepository.save(new User("first@example.com", "First", Role.USER));
        User second = userRepository.save(new User("second@example.com", "Second", Role.USER));

        tagRepository.saveAndFlush(Tag.of("java", first));
        tagRepository.saveAndFlush(Tag.of("java", second));

        assertThat(tagRepository.findAll()).hasSize(2);
        assertThatThrownBy(() -> tagRepository.saveAndFlush(Tag.of("java", first)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void filteredNotesRespectOwnerCategoryAndCaseInsensitiveTitle() {
        User owner = userRepository.save(new User("owner@example.com", "Owner", Role.USER));
        User other = userRepository.save(new User("other@example.com", "Other", Role.USER));
        Category work = categoryRepository.save(Category.of(owner, "Work"));
        Category personal = categoryRepository.save(Category.of(owner, "Personal"));

        noteRepository.save(Note.of("Alpha Guide", null, null, work, owner));
        noteRepository.save(Note.of("Alpha Personal", null, null, personal, owner));
        noteRepository.save(Note.of("Alpha Other", null, null, null, other));
        noteRepository.save(Note.of("Beta Guide", null, null, work, owner));
        noteRepository.flush();

        Page<Note> result = noteRepository.findAllFiltered(
                owner.getId(),
                work.getId(),
                null,
                "%alpha%",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Note::getTitle)
                .containsExactly("Alpha Guide");
    }

    @Test
    void nullifyingCategoryPreservesTheOwnedNote() {
        User owner = userRepository.save(new User("owner@example.com", "Owner", Role.USER));
        Category category = categoryRepository.save(Category.of(owner, "Work"));
        Note note = noteRepository.saveAndFlush(Note.of("Lifecycle", null, null, category, owner));

        noteRepository.nullifyCategory(category.getId());
        noteRepository.flush();
        entityManager.clear();

        assertThat(noteRepository.findById(note.getId()))
                .get()
                .extracting(Note::getCategory)
                .isNull();
    }
}
