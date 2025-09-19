package ru.savelevvn.spring.base.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Базовый контроллер для REST API.
 *
 * @param <T>  Тип сущности
 * @param <ID> Тип идентификатора
 * @param <S>  Тип сервиса
 */
@AllArgsConstructor
@Getter
public abstract class BaseController<T extends BaseEntity<ID>, ID extends Serializable, S extends BaseService<T, ID, ?>> {

    private final S service;

    /**
     * Получить все сущности.
     */
    @GetMapping
    public ResponseEntity<List<T>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Получить сущность по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable ID id) {
        Optional<T> entity = service.findById(id);
        return entity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создать новую сущность.
     */
    @PostMapping
    public ResponseEntity<T> create(@RequestBody T entity) {
        T savedEntity = service.save(entity);
        return ResponseEntity.ok(savedEntity);
    }

    /**
     * Обновить сущность.
     */
    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable ID id, @RequestBody T entity) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        T updatedEntity = service.save(entity);
        return ResponseEntity.ok(updatedEntity);
    }

    /**
     * Удалить сущность.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}