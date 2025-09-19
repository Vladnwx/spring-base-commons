package ru.savelevvn.spring.base.commons;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Абстрактный базовый класс для сервисов.
 * Предоставляет стандартные CRUD операции.
 * Использует Generics для гибкости.
 *
 * @param <T>  Тип сущности, должен наследоваться от BaseEntity.
 * @param <ID> Тип идентификатора сущности.
 * @param <R>  Тип репозитория, должен наследоваться от JpaRepository.
 */
@Getter
@Slf4j
public abstract class BaseService<T extends BaseEntity<ID>, ID extends Serializable, R extends BaseRepository<T, ID>> {

    /** Репозиторий для работы с сущностью. Инжектится в конкретных реализациях. */
    private final R repository;

    /**
     * Конструктор, принимающий репозиторий.
     * Реальные сервисы должны вызвать этот конструктор, передав свой репозиторий.
     *
     * @param repository Репозиторий для работы с сущностью.
     */
    public BaseService(R repository) {
        this.repository = repository;
    }

    /**
     * Сохранить сущность.
     *
     * @param entity Сущность для сохранения.
     * @return Сохраненная сущность.
     */
    @Transactional
    public T save(T entity) {
        log.debug("Сохранение сущности: {}", entity);
        return repository.save(entity);
    }

    /**
     * Сохранить список сущностей.
     *
     * @param entities Список сущностей для сохранения.
     * @return Список сохраненных сущностей.
     */
    @Transactional
    public List<T> saveAll(Iterable<T> entities) {
        log.debug("Сохранение списка сущностей");
        return repository.saveAll(entities);
    }

    /**
     * Найти сущность по ID.
     *
     * @param id ID сущности.
     * @return Optional с сущностью или пустой Optional, если не найдена.
     */
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        log.debug("Поиск сущности по ID: {}", id);
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    /**
     * Найти сущность по ID (включая удаленные).
     *
     * @param id ID сущности.
     * @return Optional с сущностью или пустой Optional, если не найдена.
     */
    @Transactional(readOnly = true)
    public Optional<T> findByIdWithDeleted(ID id) {
        log.debug("Поиск сущности по ID (включая удаленные): {}", id);
        return repository.findById(id);
    }

    /**
     * Проверить, существует ли сущность с заданным ID.
     *
     * @param id ID сущности.
     * @return true, если сущность существует.
     */
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        log.debug("Проверка существования сущности с ID: {}", id);
        return repository.findByIdAndDeletedAtIsNull(id).isPresent();
    }

    /**
     * Найти все сущности (только не удаленные).
     *
     * @return Список всех сущностей.
     */
    @Transactional(readOnly = true)
    public List<T> findAll() {
        log.debug("Получение всех сущностей (не удаленных)");
        return repository.findAllByDeletedAtIsNull();
    }

    /**
     * Найти все сущности (включая удаленные).
     *
     * @return Список всех сущностей.
     */
    @Transactional(readOnly = true)
    public List<T> findAllWithDeleted() {
        log.debug("Получение всех сущностей (включая удаленные)");
        return repository.findAll();
    }

    /**
     * Найти все удаленные сущности.
     *
     * @return Список удаленных сущностей.
     */
    @Transactional(readOnly = true)
    public List<T> findAllDeleted() {
        log.debug("Получение всех удаленных сущностей");
        return repository.findAllByDeletedAtIsNotNull();
    }

    /**
     * Удалить сущность (soft delete).
     *
     * @param entity Сущность для удаления.
     */
    @Transactional
    public void delete(T entity) {
        log.debug("Soft удаление сущности: {}", entity);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    /**
     * Удалить сущность по ID (soft delete).
     *
     * @param id ID сущности для удаления.
     */
    @Transactional
    public void deleteById(ID id) {
        log.debug("Soft удаление сущности по ID: {}", id);
        repository.softDeleteById(id, LocalDateTime.now());
    }

    /**
     * Полное удаление сущности.
     *
     * @param entity Сущность для полного удаления.
     */
    @Transactional
    public void hardDelete(T entity) {
        log.debug("Полное удаление сущности: {}", entity);
        repository.delete(entity);
    }

    /**
     * Полное удаление сущности по ID.
     *
     * @param id ID сущности для полного удаления.
     */
    @Transactional
    public void hardDeleteById(ID id) {
        log.debug("Полное удаление сущности по ID: {}", id);
        repository.findById(id).ifPresent(this::hardDelete);
    }

    /**
     * Восстановить удаленную сущность.
     *
     * @param id ID сущности для восстановления.
     */
    @Transactional
    public void restore(ID id) {
        log.debug("Восстановление сущности по ID: {}", id);
        repository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(null);
            repository.save(entity);
        });
    }

}