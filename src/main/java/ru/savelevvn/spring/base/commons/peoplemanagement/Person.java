package ru.savelevvn.spring.base.commons.peoplemanagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import ru.savelevvn.spring.base.commons.BaseEntity;

import java.time.LocalDate;

/**
 * Абстрактный класс, представляющий общую информацию о человеке.
 * Используется как базовый класс для конкретных типов пользователей (например, Employee, Student и т.д.)
 *
 * <p>Предоставляет полный набор персональных данных с валидацией и бизнес-логикой.
 * Поддерживает механизмы soft delete и аудит изменений.
 *
 * <p>Пример использования:
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "employees")
 * public class Employee extends Person {
 *     // Специфичные поля для сотрудника
 * }
 * }
 * </pre>
 *
 * @version 1.0
 * @see BaseEntity
 * @see Gender
 * @see MaritalStatus
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@ToString(callSuper = true, of = {"firstName", "lastName", "email"})
@EqualsAndHashCode(callSuper = true, of = {"email", "passportSeries", "passportNumber"})
public abstract class Person extends BaseEntity<Long> {

    /**
     * Имя человека. Обязательное поле.
     * Максимальная длина: 50 символов.
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 50, message = "Имя не должно превышать 50 символов")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * Фамилия человека. Обязательное поле.
     * Максимальная длина: 50 символов.
     */
    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(max = 50, message = "Фамилия не должна превышать 50 символов")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Отчество человека (не обязательно).
     * Максимальная длина: 50 символов.
     */
    @Size(max = 50, message = "Отчество не должно превышать 50 символов")
    @Column(name = "middle_name", length = 50)
    private String middleName;

    /**
     * Дата рождения человека.
     * Должна быть в прошлом.
     */
    @Past(message = "Дата рождения должна быть в прошлом")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Пол человека. Может принимать значения MALE или FEMALE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 6)
    private Gender gender;

    /**
     * Электронная почта человека. Уникальное значение.
     * Максимальная длина: 100 символов.
     */
    @Email(message = "Некорректный формат email")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    @Column(name = "email", unique = true, length = 100)
    private String email;

    /**
     * Номер телефона человека.
     * Максимальная длина: 20 символов.
     */
    @Size(max = 20, message = "Номер телефона не должен превышать 20 символов")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Адрес проживания человека.
     * Максимальная длина: 200 символов.
     */
    @Size(max = 200, message = "Адрес не должен превышать 200 символов")
    @Column(name = "address", length = 200)
    private String address;

    /**
     * Гражданство человека.
     * Максимальная длина: 50 символов.
     */
    @Size(max = 50, message = "Гражданство не должно превышать 50 символов")
    @Column(name = "citizenship", length = 50)
    private String citizenship;

    /**
     * Серия паспорта.
     * Максимальная длина: 10 символов.
     */
    @Size(max = 10, message = "Серия паспорта не должна превышать 10 символов")
    @Column(name = "passport_series", length = 10)
    private String passportSeries;

    /**
     * Номер паспорта.
     * Максимальная длина: 20 символов.
     */
    @Size(max = 20, message = "Номер паспорта не должен превышать 20 символов")
    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    /**
     * Дата выдачи паспорта.
     */
    @Column(name = "passport_issue_date")
    private LocalDate passportIssueDate;

    /**
     * Организация, выдавшая паспорт.
     * Максимальная длина: 200 символов.
     */
    @Size(max = 200, message = "Организация, выдавшая паспорт, не должна превышать 200 символов")
    @Column(name = "passport_issuer", length = 200)
    private String passportIssuer;

    /**
     * ИНН (Идентификационный номер налогоплательщика).
     * Максимальная длина: 12 символов.
     */
    @Size(max = 12, message = "ИНН не должен превышать 12 символов")
    @Column(name = "inn", length = 12)
    private String inn;

    /**
     * СНИЛС (Страховой номер индивидуального лицевого счёта).
     * Максимальная длина: 14 символов (включая дефисы).
     */
    @Size(max = 14, message = "СНИЛС не должен превышать 14 символов")
    @Column(name = "snils", length = 14)
    private String snils;

    /**
     * Семейное положение человека.
     * Может принимать значения: SINGLE, MARRIED, DIVORCED, WIDOWED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    /**
     * URL-адрес фотографии человека.
     * Максимальная длина: 500 символов.
     */
    @Size(max = 500, message = "URL фотографии не должен превышать 500 символов")
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /**
     * Дополнительные заметки о человеке.
     * Тип: текстовое поле большой длины.
     */
    @Lob
    @Column(name = "notes")
    private String notes;

    /**
     * Предобработка сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     */
    protected void prePersist() {
        // Нормализация данных перед сохранением
        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }
        if (this.firstName != null) {
            this.firstName = this.firstName.trim();
        }
        if (this.lastName != null) {
            this.lastName = this.lastName.trim();
        }
        if (this.middleName != null) {
            this.middleName = this.middleName.trim();
        }
    }

    /**
     * Валидация сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @throws IllegalArgumentException если сущность не прошла валидацию
     */
    protected void validate() {
        // Проверка корректности паспортных данных
        if (passportSeries != null && passportNumber != null) {
            if (passportSeries.length() + passportNumber.length() > 30) {
                throw new IllegalArgumentException("Серия и номер паспорта слишком длинные");
            }
        }

        // Проверка возраста при наличии даты рождения
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }

    /**
     * Получение полного имени человека (Фамилия Имя Отчество).
     *
     * @return Полное имя человека.
     */
    @Transient
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (lastName != null) fullName.append(lastName).append(" ");
        if (firstName != null) fullName.append(firstName).append(" ");
        if (middleName != null) fullName.append(middleName);
        return fullName.toString().trim();
    }

    /**
     * Получение формального обращения к человеку.
     *
     * @return Формальное обращение (Г-н/Г-жа + Фамилия)
     */
    @Transient
    public String getFormalAddress() {
        StringBuilder address = new StringBuilder();
        if (gender != null) {
            address.append(gender == Gender.MALE ? "Г-н " : "Г-жа ");
        }
        if (lastName != null) {
            address.append(lastName);
        }
        return address.toString().trim();
    }

    /**
     * Проверяет, достиг ли человек совершеннолетия.
     *
     * @return true, если человеку 18 лет или больше.
     */
    @Transient
    public boolean isAdult() {
        if (birthDate == null) return false;
        return !birthDate.plusYears(18).isAfter(LocalDate.now());
    }

    /**
     * Проверяет, является ли человек пенсионером.
     * Мужчины - после 65 лет, женщины - после 60 лет.
     *
     * @return true, если человек является пенсионером.
     */
    @Transient
    public boolean isPensioner() {
        if (birthDate == null || gender == null) return false;

        LocalDate pensionAge = gender == Gender.MALE ?
                birthDate.plusYears(65) : birthDate.plusYears(60);

        return !pensionAge.isAfter(LocalDate.now());
    }

    /**
     * Получает возраст человека в годах.
     *
     * @return Возраст в годах или -1, если дата рождения не указана.
     */
    @Transient
    public int getAge() {
        if (birthDate == null) return -1;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    /**
     * Проверяет, является ли email корпоративным.
     *
     * @return true, если email содержит домен организации.
     */
    @Transient
    public boolean isCorporateEmail() {
        if (email == null) return false;
        // Может быть переопределен в подклассах
        return email.contains("@company.com") || email.contains("@organization.com");
    }

    /**
     * Проверяет, заполнены ли паспортные данные полностью.
     *
     * @return true, если все паспортные данные заполнены.
     */
    @Transient
    public boolean isPassportComplete() {
        return passportSeries != null && !passportSeries.isEmpty() &&
                passportNumber != null && !passportNumber.isEmpty() &&
                passportIssueDate != null &&
                passportIssuer != null && !passportIssuer.isEmpty();
    }

    /**
     * Проверяет, заполнены ли контактные данные.
     *
     * @return true, если хотя бы один контакт заполнен.
     */
    @Transient
    public boolean hasContactInfo() {
        return (email != null && !email.isEmpty()) ||
                (phone != null && !phone.isEmpty()) ||
                (address != null && !address.isEmpty());
    }

    /**
     * Получает возрастную категорию человека.
     *
     * @return Возрастная категория.
     */
    @Transient
    public AgeCategory getAgeCategory() {
        int age = getAge();
        if (age < 0) return AgeCategory.UNKNOWN;
        if (age < 18) return AgeCategory.CHILD;
        if (age < 30) return AgeCategory.YOUNG_ADULT;
        if (age < 50) return AgeCategory.ADULT;
        if (age < 65) return AgeCategory.MIDDLE_AGED;
        return AgeCategory.SENIOR;
    }

    /**
     * Перечисление возрастных категорий.
     */
    public enum AgeCategory {
        UNKNOWN("Неизвестно"),
        CHILD("Ребенок"),
        YOUNG_ADULT("Молодой взрослый"),
        ADULT("Взрослый"),
        MIDDLE_AGED("Среднего возраста"),
        SENIOR("Пожилой");

        private final String description;

        AgeCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}