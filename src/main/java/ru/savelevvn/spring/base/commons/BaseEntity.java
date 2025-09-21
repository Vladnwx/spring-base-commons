package ru.savelevvn.spring.base.commons;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Базовая сущность с общими полями для всех сущностей.
 */

@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@SuperBuilder
@ToString(of = {"id", "createdAt", "lastModifiedAt", "version"})
public abstract class BaseEntity<ID>{

    /**
     * Уникальный идентификатор сущности.
     * Генерируется автоматически базой данных.
     */
    @Id
    //@GeneratedValue(strategy = GenerationType.UUID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@Column(name = "id", nullable = false, updatable = false, columnDefinition = "VARCHAR(36)")
    @Column(name = "id", nullable = false, updatable = false)
    private ID id;

    /**
     * Дата и время создания сущности.
     * Заполняется автоматически при создании.
     * Не может быть изменена после создания.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего изменения сущности.
     * Автоматически обновляется при каждом изменении.
     */
    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    /**
     * Дата и время логического удаления сущности.
     * Используется для soft delete - сущность помечается как удаленная,
     * но физически не удаляется из базы данных.
     * null означает, что сущность не удалена.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Версия сущности для оптимистической блокировки.
     * Используется для предотвращения конфликтов при одновременном
     * изменении одной и той же сущности несколькими пользователями.
     */
    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * Имя пользователя, создавшего сущность.
     * Заполняется автоматически Spring Security auditing.
     * Не может быть изменено после создания.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Имя пользователя, последним изменившего сущность.
     * Автоматически обновляется при каждом изменении.
     */
    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;


    /**
     * Проверяет, была ли сущность логически удалена.
     *
     * @return true, если сущность удалена (deletedAt не null), иначе false
     */
    public boolean isDeleted() {

        return deletedAt != null;
    }

    /**
     * Помечает сущность как логически удаленную.
     * Устанавливает текущее время в поле deletedAt.
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Восстанавливает сущность после логического удаления.
     * Устанавливает поле deletedAt в null.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Проверяет, была ли сущность изменена после создания.
     *
     * @return true, если сущность была изменена, иначе false
     */
    public boolean isModified() {
        return lastModifiedAt != null && !lastModifiedAt.equals(createdAt);
    }

    /**
     * Получает время жизни сущности (разницу между созданием и последним изменением).
     *
     * @return количество секунд между созданием и последним изменением, или 0 если не изменялась
     */
    public long getLifetimeSeconds() {
        if (lastModifiedAt != null) {
            return java.time.Duration.between(createdAt, lastModifiedAt).getSeconds();
        }
        return 0;
    }

    /**
     * Проверяет, является ли сущность новой (еще не сохраненной в БД).
     *
     * @return true, если сущность новая (id равен null), иначе false
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * Сравнивает сущности по их идентификаторам.
     *
     * @param o объект для сравнения
     * @return true, если объекты равны по идентификатору, иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity<?> that)) return false;
        return getClass().equals(that.getClass()) &&
                id != null && id.equals(that.id);
    }

    /**
     * Вычисляет хэш-код сущности на основе идентификатора.
     *
     * @return хэш-код сущности
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}