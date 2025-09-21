package ru.savelevvn.spring.base.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Optional;

/**
 * Базовый контроллер для REST API с расширенной функциональностью.
 * Предоставляет стандартные CRUD операции, пагинацию, сортировку и обработку ошибок.
 *
 * <p>Поддерживаемая функциональность:
 * <ul>
 *   <li>Стандартные CRUD операции (GET, POST, DELETE)</li>
 *   <li>Пагинация и сортировка</li>
 *   <li>Обработка ошибок с HTTP статусами</li>
 *   <li>Валидация входных данных</li>
 *   <li>Полное логирование операций</li>
 *   <li>Поддержка soft delete и восстановления</li>
 * </ul>
 *
 * <p>Использует паттерн Template Method для гибкой настройки поведения
 * в конкретных реализациях контроллеров.
 *
 * @param <T>  Тип сущности, должен наследоваться от BaseEntity
 * @param <ID> Тип идентификатора сущности
 * @param <S>  Тип сервиса, должен наследоваться от BaseService
 * @author Savelev Vladimir
 * @version 1.0
 */
@Slf4j
@AllArgsConstructor
@Getter
public abstract class BaseController<T extends BaseEntity<ID>, ID extends Serializable, S extends BaseService<T, ID, ?>> {

    protected final S service;

    /**
     * Предобработка сущности перед созданием.
     * Может быть переопределена в подклассах для реализации специфической логики.
     *
     * @param entity сущность для предобработки
     * @return обработанная сущность
     */
    protected T preCreate(T entity) {
        log.trace("Предобработка сущности перед созданием: {}", entity);
        return entity;
    }

    /**
     * Предобработка сущности перед удалением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     *
     * @param entity сущность для предобработки
     * @return обработанная сущность
     */
    protected T preDelete(T entity) {
        log.trace("Предобработка сущности перед удалением: {}", entity);
        return entity;
    }

    /**
     * Валидация сущности перед созданием.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если сущность не прошла валидацию
     */
    protected void validateCreate(T entity) {
        log.trace("Валидация сущности перед созданием: {}", entity);
        // Базовая валидация может быть реализована в подклассах
    }

    /**
     * Валидация сущности перед обновлением.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @param id идентификатор сущности
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если сущность не прошла валидацию
     */
    protected void validateUpdate(ID id, T entity) {
        log.trace("Валидация сущности перед обновлением ID {}: {}", id, entity);
        // Базовая валидация может быть реализована в подклассах
    }

    /**
     * Валидация идентификатора.
     * Может быть переопределена в подклассах для реализации специфической валидации.
     *
     * @param id идентификатор для валидации
     * @throws IllegalArgumentException если идентификатор не прошел валидацию
     */
    protected void validateId(ID id) {
        log.trace("Валидация идентификатора: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор не может быть null");
        }
    }

    /**
     * Постобработка результата перед отправкой клиенту.
     * Может быть переопределена в подклассах для реализации дополнительной логики.
     *
     * @param entity сущность для постобработки
     * @return обработанная сущность
     */
    protected T postProcess(T entity) {
        log.trace("Постобработка сущности перед отправкой: {}", entity);
        return entity;
    }

    /**
     * Постобработка страницы результатов перед отправкой клиенту.
     * Может быть переопределена в подклассах для реализации дополнительной логики.
     *
     * @param page страница с сущностями для постобработки
     * @return обработанная страница
     */
    protected Page<T> postProcess(Page<T> page) {
        log.trace("Постобработка страницы перед отправкой, элементов: {}", page.getNumberOfElements());
        return page;
    }

    /**
     * Обработка исключения.
     * Может быть переопределена в подклассах для реализации специфической обработки ошибок.
     *
     * @param e исключение для обработки
     * @param operation описание операции, в которой произошла ошибка
     * @return ResponseEntity с соответствующим HTTP статусом
     */
    protected ResponseEntity<?> handleException(Exception e, String operation) {
        log.error("Ошибка при выполнении операции '{}': ", operation, e);
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Получить все сущности с пагинацией и сортировкой.
     *
     * <p>Параметры запроса:
     * <ul>
     *   <li>page - номер страницы (по умолчанию 0)</li>
     *   <li>size - размер страницы (по умолчанию 20)</li>
     *   <li>sort - поле для сортировки (по умолчанию id)</li>
     *   <li>direction - направление сортировки (ASC/DESC, по умолчанию ASC)</li>
     * </ul>
     *
     * @param page номер страницы (0-based)
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки (asc/desc)
     * @return страница с сущностями
     */
    @GetMapping
    public ResponseEntity<Page<T>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Получение всех сущностей: страница={}, размер={}, сортировка={} {}", page, size, sort, direction);

        try {
            // Валидация параметров пагинации
            if (page < 0) {
                log.warn("Некорректный номер страницы: {}", page);
                return ResponseEntity.badRequest().build();
            }
            if (size <= 0 || size > 100) {
                log.warn("Некорректный размер страницы: {}", size);
                return ResponseEntity.badRequest().build();
            }

            // Создание объекта пагинации
            Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<T> entities = service.findAll(pageable);
            Page<T> processedEntities = postProcess(entities);

            log.info("Успешно получено {} сущностей, страница {}/{}",
                    processedEntities.getNumberOfElements(), page, processedEntities.getTotalPages());

            return ResponseEntity.ok(processedEntities);
        } catch (Exception e) {
            return (ResponseEntity<Page<T>>) handleException(e, "получение всех сущностей");
        }
    }

    /**
     * Получить сущность по ID.
     *
     * @param id идентификатор сущности
     * @return сущность или 404 если не найдена
     */
    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable ID id) {
        log.debug("Получение сущности по ID: {}", id);

        try {
            validateId(id);

            Optional<T> entity = service.findById(id);
            if (entity.isPresent()) {
                T processedEntity = postProcess(entity.get());
                log.info("Сущность найдена по ID: {}", id);
                return ResponseEntity.ok(processedEntity);
            } else {
                log.info("Сущность не найдена по ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return (ResponseEntity<T>) handleException(e, "получение сущности по ID " + id);
        }
    }

    /**
     * Создать новую сущность.
     *
     * @param entity сущность для создания
     * @return созданная сущность
     */
    @PostMapping
    public ResponseEntity<T> create(@Valid @RequestBody T entity) {
        log.debug("Создание новой сущности: {}", entity);

        try {
            // Проверяем, что сущность новая (не имеет ID)
            if (entity.getId() != null) {
                log.warn("Попытка создания сущности с существующим ID: {}", entity.getId());
                return ResponseEntity.badRequest().build();
            }

            // Валидация
            validateCreate(entity);

            // Предобработка
            T processedEntity = preCreate(entity);

            // Сохранение
            T savedEntity = service.save(processedEntity);

            // Постобработка
            T resultEntity = postProcess(savedEntity);

            log.info("Сущность успешно создана с ID: {}", resultEntity.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(resultEntity);
        } catch (Exception e) {
            return (ResponseEntity<T>) handleException(e, "создание сущности");
        }
    }

    /**
     * Удалить сущность (soft delete).
     *
     * @param id идентификатор сущности
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        log.debug("Удаление сущности с ID: {}", id);

        try {
            validateId(id);

            if (!service.existsById(id)) {
                log.info("Попытка удаления несуществующей сущности с ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            Optional<T> entityOpt = service.findById(id);
            if (entityOpt.isPresent()) {
                T entity = preDelete(entityOpt.get());
                service.delete(entity);
            } else {
                service.deleteById(id);
            }

            log.info("Сущность успешно удалена (soft delete) с ID: {}", id);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return (ResponseEntity<Void>) handleException(e, "удаление сущности с ID " + id);
        }
    }

    /**
     * Полное удаление сущности.
     *
     * @param id идентификатор сущности
     * @return 204 No Content
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable ID id) {
        log.debug("Полное удаление сущности с ID: {}", id);

        try {
            validateId(id);

            if (!service.existsById(id)) {
                log.info("Попытка полного удаления несуществующей сущности с ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            service.hardDeleteById(id);
            log.info("Сущность успешно полностью удалена с ID: {}", id);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return (ResponseEntity<Void>) handleException(e, "полное удаление сущности с ID " + id);
        }
    }

    /**
     * Восстановить удаленную сущность.
     *
     * @param id идентификатор сущности
     * @return восстановленная сущность или 404 если не найдена
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<T> restore(@PathVariable ID id) {
        log.debug("Восстановление сущности с ID: {}", id);

        try {
            validateId(id);

            // Проверяем, существует ли удаленная сущность
            Optional<T> deletedEntityOpt = service.findByIdWithDeleted(id);
            if (deletedEntityOpt.isEmpty()) {
                log.info("Попытка восстановления несуществующей сущности с ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            T deletedEntity = deletedEntityOpt.get();
            if (!deletedEntity.isDeleted()) {
                log.info("Попытка восстановления неудаленной сущности с ID: {}", id);
                return ResponseEntity.badRequest().build();
            }

            service.restore(id);

            Optional<T> restoredEntity = service.findById(id);
            if (restoredEntity.isPresent()) {
                T processedEntity = postProcess(restoredEntity.get());
                log.info("Сущность успешно восстановлена с ID: {}", id);
                return ResponseEntity.ok(processedEntity);
            } else {
                log.warn("Не удалось восстановить сущность с ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return (ResponseEntity<T>) handleException(e, "восстановление сущности с ID " + id);
        }
    }

    /**
     * Получить количество неудаленных сущностей.
     *
     * @return количество неудаленных сущностей
     */
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        log.debug("Получение количества сущностей");

        try {
            long count = service.count();
            log.info("Получено количество сущностей: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return (ResponseEntity<Long>) handleException(e, "получение количества сущностей");
        }
    }

    /**
     * Проверить существование сущности по ID.
     *
     * @param id идентификатор сущности
     * @return true если сущность существует, false если нет
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable ID id) {
        log.debug("Проверка существования сущности с ID: {}", id);

        try {
            validateId(id);
            boolean exists = service.existsById(id);
            log.info("Сущность с ID {} {}", id, exists ? "существует" : "не существует");
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return (ResponseEntity<Boolean>) handleException(e, "проверка существования сущности с ID " + id);
        }
    }

    /**
     * Получить все удаленные сущности с пагинацией.
     *
     * @param page номер страницы (0-based)
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки (asc/desc)
     * @return страница с удаленными сущностями
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<T>> getDeleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Получение удаленных сущностей: страница={}, размер={}, сортировка={} {}", page, size, sort, direction);

        try {
            if (page < 0) {
                log.warn("Некорректный номер страницы: {}", page);
                return ResponseEntity.badRequest().build();
            }
            if (size <= 0 || size > 100) {
                log.warn("Некорректный размер страницы: {}", size);
                return ResponseEntity.badRequest().build();
            }

            Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<T> entities = service.findAllDeleted(pageable);
            Page<T> processedEntities = postProcess(entities);

            log.info("Успешно получено {} удаленных сущностей, страница {}/{}",
                    processedEntities.getNumberOfElements(), page, processedEntities.getTotalPages());

            return ResponseEntity.ok(processedEntities);
        } catch (Exception e) {
            return (ResponseEntity<Page<T>>) handleException(e, "получение удаленных сущностей");
        }
    }

    /**
     * Обновить существующую сущность.
     * <strong>Должен быть переопределен в конкретных контроллерах</strong>
     * с учетом специфики сущности.
     *
     * @param id идентификатор сущности
     * @param entity обновленные данные сущности
     * @return обновленная сущность или 501 Not Implemented
     */
    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable ID id, @Valid @RequestBody T entity) {
        log.warn("Метод PUT не реализован в базовом контроллере. ID: {}", id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Частично обновить существующую сущность.
     * <strong>Должен быть переопределен в конкретных контроллерах</strong>
     * с учетом специфики сущности.
     *
     * @param id идентификатор сущности
     * @param entity обновленные данные сущности
     * @return обновленная сущность или 501 Not Implemented
     */
    @PatchMapping("/{id}")
    public ResponseEntity<T> partialUpdate(@PathVariable ID id, @Valid @RequestBody T entity) {
        log.warn("Метод PATCH не реализован в базовом контроллере. ID: {}", id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}