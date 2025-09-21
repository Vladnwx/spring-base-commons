package ru.savelevvn.spring.base.commons;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Предоставляет стандартные CRUD операции и дополнительные методы для работы
 * с механизмом soft delete (логическое удаление).
 *
 * <p>Наследует все стандартные методы JpaRepository:
 * <ul>
 *   <li>findAll() - получение всех неудаленных сущностей</li>
 *   <li>findById(ID) - поиск сущности по идентификатору</li>
 *   <li>save(T) - сохранение сущности</li>
 *   <li>delete(T) - физическое удаление сущности</li>
 * </ul>
 *
 * <p>Дополнительно предоставляет методы для работы с soft delete:
 * <ul>
 *   <li>findAllByDeletedAtIsNull() - поиск всех неудаленных сущностей</li>
 *   <li>findAllByDeletedAtIsNotNull() - поиск всех удаленных сущностей</li>
 *   <li>findByIdAndDeletedAtIsNull(ID) - поиск неудаленной сущности по ID</li>
 *   <li>softDeleteById(ID) - логическое удаление сущности</li>
 * </ul>
 *
 * @param <T>  Тип сущности, должен наследоваться от BaseEntity
 * @param <ID> Тип идентификатора сущности
 * @version 1.0
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID extends Serializable>
        extends JpaRepository<T, ID> {

    // JpaRepository уже предоставляет findAll, findById, save, delete и другие стандартные методы.

    /**
     * Находит все сущности, которые не были логически удалены.
     *
     * @return список всех неудаленных сущностей
     */
    List<T> findAllByDeletedAtIsNull();

    /**
     * Находит все сущности, которые не были логически удалены с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница с неудаленными сущностями
     */
    Page<T> findAllByDeletedAtIsNull(Pageable pageable);

    /**
     * Находит все сущности, которые были логически удалены.
     *
     * @return список всех удаленных сущностей
     */
    List<T> findAllByDeletedAtIsNotNull();

    /**
     * Находит все сущности, которые были логически удалены с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница с удаленными сущностями
     */
    Page<T> findAllByDeletedAtIsNotNull(Pageable pageable);

    /**
     * Находит сущность по идентификатору, если она не была логически удалена.
     *
     * @param id идентификатор сущности
     * @return Optional с сущностью, если найдена и не удалена, иначе пустой Optional
     */
    Optional<T> findByIdAndDeletedAtIsNull(ID id);

    /**
     * Выполняет логическое удаление сущности по идентификатору.
     * Устанавливает текущее время в поле deletedAt сущности.
     *
     * @param id идентификатор сущности для удаления
     * @return количество затронутых записей (0 или 1)
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.deletedAt IS NULL")
    int softDeleteById(@Param("id") ID id);

    /**
     * Восстанавливает логически удаленную сущность по идентификатору.
     * Устанавливает поле deletedAt в null.
     *
     * @param id идентификатор сущности для восстановления
     * @return количество затронутых записей (0 или 1)
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deletedAt = NULL WHERE e.id = :id AND e.deletedAt IS NOT NULL")
    int restoreById(@Param("id") ID id);

    /**
     * Проверяет, существует ли сущность с указанным идентификатором
     * и не была ли она логически удалена.
     *
     * @param id идентификатор сущности
     * @return true, если сущность существует и не удалена, иначе false
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") ID id);

    /**
     * Подсчитывает количество неудаленных сущностей.
     *
     * @return количество неудаленных сущностей
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    long countNotDeleted();

    /**
     * Подсчитывает количество удаленных сущностей.
     *
     * @return количество удаленных сущностей
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deletedAt IS NOT NULL")
    long countDeleted();

    /**
     * Находит все сущности, созданные после указанной даты и времени.
     *
     * @param createdAt дата и время создания
     * @return список сущностей, созданных после указанной даты
     */
    List<T> findAllByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * Находит все сущности, измененные после указанной даты и времени.
     *
     * @param lastModifiedAt дата и время последнего изменения
     * @return список сущностей, измененных после указанной даты
     */
    List<T> findAllByLastModifiedAtAfter(LocalDateTime lastModifiedAt);

    /**
     * Находит все сущности, созданные указанным пользователем.
     *
     * @param createdBy имя пользователя-создателя
     * @return список сущностей, созданных указанным пользователем
     */
    List<T> findAllByCreatedBy(String createdBy);

    /**
     * Находит все сущности, измененные указанным пользователем.
     *
     * @param lastModifiedBy имя пользователя, последним изменившего сущность
     * @return список сущностей, измененных указанным пользователем
     */
    List<T> findAllByLastModifiedBy(String lastModifiedBy);
}