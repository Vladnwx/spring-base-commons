package ru.savelevvn.spring.base.commons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Базовый репозиторий для всех сущностей, наследующихся от BaseEntity.
 * Предоставляет стандартные CRUD операции.
 *
 * @param <T>  Тип сущности, должен наследоваться от BaseEntity.
 * @param <ID> Тип идентификатора сущности.
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID extends Serializable>
        extends JpaRepository<T, ID> {

    // JpaRepository уже предоставляет findAll, findById, save, delete и другие стандартные методы.

    List<T> findAllByDeletedAtIsNull();
    List<T> findAllByDeletedAtIsNotNull();
    Optional<T> findByIdAndDeletedAtIsNull(ID id);

    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deletedAt = :deletedAt WHERE e.id = :id")
    void softDeleteById(@Param("id") ID id, @Param("deletedAt") LocalDateTime deletedAt);

}