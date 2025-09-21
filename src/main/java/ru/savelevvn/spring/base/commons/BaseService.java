package ru.savelevvn.spring.base.commons;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Абстрактный базовый класс для сервисов с расширенной функциональностью.
 * Предоставляет стандартные CRUD операции, пагинацию, валидацию и логирование.
 *
 * <p>Реализует паттерн Template Method для гибкой настройки поведения
 * в конкретных реализациях сервисов.
 *
 * <p>Поддерживаемая функциональность:
 * <ul>
 *   <li>Стандартные CRUD операции с транзакционной безопасностью</li>
 *   <li>Soft delete (логическое удаление) и восстановление</li>
 *   <li>Пагинация и сортировка</li>
 *   <li>Валидация входных данных</li>
 *   <li>Полное логирование операций</li>
 *   <li>Обработка исключений и бизнес-логики</li>
 * </ul>
 *
 * @param <T>  Тип сущности, должен наследоваться от BaseEntity
 * @param <ID> Тип идентификатора сущности
 * @param <R>  Тип репозитория, должен наследоваться от BaseRepository
 * @version 1.0
 */
@Getter
@Slf4j
@Transactional(readOnly = true)
public abstract class BaseService<T extends BaseEntity<ID>, ID extends Serializable, R extends BaseRepository<T, ID>> {

    /** Репозиторий для работы с сущностью. Инжектится в конкретных реализациях. */
    protected final R repository;

    /**
     * Конструктор, принимающий репозиторий.
     * Реальные сервисы должны вызвать этот конструктор, передав свой репозиторий.
     *
     * @param repository Репозиторий для работы с сущностью
     */
    public BaseService(R repository) {
        this.repository = repository;
    }

    /**
     * Предобработка сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     *
     * @param entity сущность для предобработки
     * @return обработанная сущность
     */
    protected T preSave(T entity) {
        log.trace("Предобработка сущности перед сохранением: {}", entity);
        return entity;
    }

    /**
     * Валидация сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если сущность не прошла валидацию
     */
    protected void validate(T entity) {
        log.trace("Валидация сущности: {}", entity);
        // Базовая валидация может быть реализована в подклассах
    }

    /**
     * Постобработка сущности после сохранения.
     * Может быть переопределена в подклассах для реализации дополнительной логики.
     *
     * @param entity сохраненная сущность
     * @return обработанная сущность
     */
    protected T postSave(T entity) {
        log.trace("Постобработка сущности после сохранения: {}", entity);
        return entity;
    }

    /**
     * Сохранить сущность с полной обработкой жизненного цикла.
     * Выполняет валидацию, предобработку, сохранение и постобработку.
     *
     * @param entity Сущность для сохранения
     * @return Сохраненная сущность
     * @throws IllegalArgumentException если сущность не прошла валидацию
     */
    @Transactional
    public T save(T entity) {
        log.debug("Сохранение сущности: {}", entity);

        // Валидация входных данных
        validate(entity);

        // Предобработка
        T processedEntity = preSave(entity);

        // Сохранение
        T savedEntity = repository.save(processedEntity);
        log.info("Сущность успешно сохранена с ID: {}", savedEntity.getId());

        // Постобработка
        return postSave(savedEntity);
    }

    /**
     * Сохранить список сущностей с обработкой ошибок.
     *
     * @param entities Список сущностей для сохранения
     * @return Список сохраненных сущностей
     * @throws IllegalArgumentException если одна из сущностей не прошла валидацию
     */
    @Transactional
    public List<T> saveAll(Iterable<T> entities) {
        log.debug("Сохранение списка сущностей");

        entities.forEach(this::validate);

        List<T> savedEntities = repository.saveAll(entities);
        log.info("Успешно сохранено {} сущностей", savedEntities.size());

        return savedEntities;
    }

    /**
     * Найти сущность по ID с проверкой на удаление.
     *
     * @param id ID сущности
     * @return Optional с сущностью или пустой Optional, если не найдена или удалена
     * @throws IllegalArgumentException если ID равен null
     */
    public Optional<T> findById(ID id) {
        if (id == null) {
            log.warn("Попытка поиска сущности с null ID");
            throw new IllegalArgumentException("ID не может быть null");
        }

        log.debug("Поиск сущности по ID: {}", id);
        Optional<T> entity = repository.findByIdAndDeletedAtIsNull(id);

        if (entity.isPresent()) {
            log.trace("Сущность найдена по ID: {}", id);
        } else {
            log.debug("Сущность не найдена по ID: {} или была удалена", id);
        }

        return entity;
    }

    /**
     * Найти сущность по ID (включая удаленные).
     *
     * @param id ID сущности
     * @return Optional с сущностью или пустой Optional, если не найдена
     * @throws IllegalArgumentException если ID равен null
     */
    public Optional<T> findByIdWithDeleted(ID id) {
        if (id == null) {
            log.warn("Попытка поиска сущности с null ID");
            throw new IllegalArgumentException("ID не может быть null");
        }

        log.debug("Поиск сущности по ID (включая удаленные): {}", id);
        Optional<T> entity = repository.findById(id);

        if (entity.isPresent()) {
            log.trace("Сущность найдена по ID: {}", id);
        } else {
            log.debug("Сущность не найдена по ID: {}", id);
        }

        return entity;
    }

    /**
     * Проверить, существует ли сущность с заданным ID и не удалена ли она.
     *
     * @param id ID сущности
     * @return true, если сущность существует и не удалена
     * @throws IllegalArgumentException если ID равен null
     */
    public boolean existsById(ID id) {
        if (id == null) {
            log.warn("Попытка проверки существования сущности с null ID");
            throw new IllegalArgumentException("ID не может быть null");
        }

        log.debug("Проверка существования сущности с ID: {}", id);
        boolean exists = repository.existsByIdAndNotDeleted(id);
        log.trace("Сущность с ID {} {} существует", id, exists ? "" : "не");

        return exists;
    }

    /**
     * Найти все сущности (только не удаленные) с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница с сущностями
     */
    public Page<T> findAll(Pageable pageable) {
        log.debug("Получение страницы сущностей (не удаленных), страница: {}, размер: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAllByDeletedAtIsNull(pageable);
    }

    /**
     * Найти все сущности (только не удаленные).
     *
     * @return Список всех сущностей
     */
    public List<T> findAll() {
        log.debug("Получение всех сущностей (не удаленных)");
        List<T> entities = repository.findAllByDeletedAtIsNull();
        log.trace("Получено {} сущностей", entities.size());
        return entities;
    }

    /**
     * Найти все сущности (включая удаленные).
     *
     * @return Список всех сущностей
     */
    public List<T> findAllWithDeleted() {
        log.debug("Получение всех сущностей (включая удаленные)");
        List<T> entities = repository.findAll();
        log.trace("Получено {} сущностей (включая удаленные)", entities.size());
        return entities;
    }

    /**
     * Найти все удаленные сущности.
     *
     * @return Список удаленных сущностей
     */
    public List<T> findAllDeleted() {
        log.debug("Получение всех удаленных сущностей");
        List<T> entities = repository.findAllByDeletedAtIsNotNull();
        log.trace("Получено {} удаленных сущностей", entities.size());
        return entities;
    }

    /**
     * Найти все удаленные сущности с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница с удаленными сущностями
     */
    public Page<T> findAllDeleted(Pageable pageable) {
        log.debug("Получение страницы удаленных сущностей, страница: {}, размер: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAllByDeletedAtIsNotNull(pageable);
    }

    /**
     * Получить количество неудаленных сущностей.
     *
     * @return количество неудаленных сущностей
     */
    public long count() {
        long count = repository.countNotDeleted();
        log.debug("Количество неудаленных сущностей: {}", count);
        return count;
    }

    /**
     * Получить количество всех сущностей (включая удаленные).
     *
     * @return общее количество сущностей
     */
    public long countWithDeleted() {
        long count = repository.count();
        log.debug("Общее количество сущностей (включая удаленные): {}", count);
        return count;
    }

    /**
     * Получить количество удаленных сущностей.
     *
     * @return количество удаленных сущностей
     */
    public long countDeleted() {
        long count = repository.countDeleted();
        log.debug("Количество удаленных сущностей: {}", count);
        return count;
    }

    /**
     * Удалить сущность (soft delete) с проверкой существования.
     *
     * @param entity Сущность для удаления
     * @throws IllegalArgumentException если сущность null или не существует
     */
    @Transactional
    public void delete(T entity) {
        if (entity == null) {
            log.warn("Попытка удаления null сущности");
            throw new IllegalArgumentException("Сущность не может быть null");
        }

        if (entity.getId() == null) {
            log.warn("Попытка удаления сущности без ID");
            throw new IllegalArgumentException("Невозможно удалить сущность без ID");
        }

        log.debug("Soft удаление сущности: {}", entity);

        // Проверяем, что сущность существует
        if (!existsById(entity.getId())) {
            log.warn("Попытка удаления несуществующей сущности с ID: {}", entity.getId());
            throw new IllegalArgumentException("Сущность с ID " + entity.getId() + " не существует");
        }

        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
        log.info("Сущность успешно удалена (soft delete) с ID: {}", entity.getId());
    }

    /**
     * Удалить сущность по ID (soft delete).
     *
     * @param id ID сущности для удаления
     * @throws IllegalArgumentException если ID равен null
     */
    @Transactional
    public void deleteById(ID id) {
        if (id == null) {
            log.warn("Попытка удаления сущности с null ID");
            throw new IllegalArgumentException("ID не может быть null");
        }

        log.debug("Soft удаление сущности по ID: {}", id);
        int affected = repository.softDeleteById(id);

        if (affected > 0) {
            log.info("Сущность успешно удалена (soft delete) с ID: {}", id);
        } else {
            log.warn("Сущность с ID {} не найдена или уже удалена", id);
        }
    }

    /**
     * Полное удаление сущности с проверкой.
     *
     * @param entity Сущность для полного удаления
     * @throws IllegalArgumentException если сущность null
     */
    @Transactional
    public void hardDelete(T entity) {
        if (entity == null) {
            log.warn("Попытка полного удаления null сущности");
            throw new IllegalArgumentException("Сущность не может быть null");
        }

        log.debug("Полное удаление сущности: {}", entity);
        repository.delete(entity);
        log.info("Сущность успешно полностью удалена с ID: {}",
                entity.getId() != null ? entity.getId() : "unknown");
    }

    /**
     * Полное удаление сущности по ID.
     *
     * @param id ID сущности для полного удаления
     * @throws IllegalArgumentException если ID равен null
     */
    @Transactional
    public void hardDeleteById(ID id) {
        if (id == null) {
            log.warn("Попытка полного удаления сущности с null ID");
            throw new IllegalArgumentException("ID не может быть null");
        }

        log.debug("Полное удаление сущности по ID: {}", id);
        Optional<T> entity = repository.findById(id);

        if (entity.isPresent()) {
            repository.delete(entity.get());
            log.info("Сущность успешно полностью удалена с ID: {}", id);
        } else {
            log.warn("Сущность с ID {} не найдена для полного удаления", id);
        }
    }

    /**
     * Восстановить удаленную сущность.
     *
     * @param id ID сущности для восстановления
     * @throws IllegalArgumentException если ID равен null
     */
    @Transactional
    public void restore(ID id) {
        if (id == null) {
            log.warn("Попытка восстановления сущности с null ID");
            throw new IllegalArgumentException("ID не может быть null");
        }

        log.debug("Восстановление сущности по ID: {}", id);
        int affected = repository.restoreById(id);

        if (affected > 0) {
            log.info("Сущность успешно восстановлена с ID: {}", id);
        } else {
            log.warn("Сущность с ID {} не найдена или не была удалена", id);
        }
    }

    /**
     * Проверить, удалена ли сущность.
     *
     * @param id ID сущности
     * @return true, если сущность удалена, false если не удалена или не существует
     * @throws IllegalArgumentException если ID равен null
     */
    public boolean isDeleted(ID id) {
        if (id == null) {
            log.warn("Попытка проверки удаления сущности с null ID");
            throw new IllegalArgumentException("ID не может быть null");
        }

        return findByIdWithDeleted(id)
                .map(BaseEntity::isDeleted)
                .orElse(false);
    }
}