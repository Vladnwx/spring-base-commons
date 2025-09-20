package ru.savelevvn.spring.base.commons.peoplemanagement.employee;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.savelevvn.spring.base.commons.peoplemanagement.Person;

import java.time.LocalDate;

/**
 * Абстрактный класс, представляющий сотрудника организации.
 * Расширяет класс {@link Person}, добавляя информацию, специфичную для работников.
 *
 * <p>Используется как базовый класс для конкретных типов сотрудников.
 * Все перечисляемые типы (Gender, MaritalStatus, WorkSchedule, EmploymentType)
 * хранятся в базе данных в виде строковых значений.
 *
 * @since 1.0
 * @see Person
 * @see WorkSchedule
 * @see EmploymentType
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class Employee extends Person {

    /**
     * Уникальный идентификатор сотрудника в организации.
     * Используется для внутренней идентификации работника.
     * Максимальная длина: 20 символов.
     *
     * <p>Пример: {@code "EMP001234"}, {@code "DEV567890"}
     */
    @Column(name = "employee_id", unique = true, length = 20)
    private String employeeId;

    /**
     * Дата приема на работу.
     * Обязательное поле для активных сотрудников.
     *
     * <p>Используется для расчета стажа работы, отпусков и других HR-показателей.
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /**
     * Дата увольнения сотрудника.
     * Заполняется только для уволенных сотрудников.
     *
     * <p>Если null, сотрудник считается действующим.
     */
    @Column(name = "termination_date")
    private LocalDate terminationDate;

    /**
     * Причина увольнения сотрудника.
     * Заполняется при увольнении сотрудника.
     * Максимальная длина: 500 символов.
     */
    @Column(name = "termination_reason", length = 500)
    private String terminationReason;

    /**
     * Должность сотрудника.
     * Максимальная длина: 100 символов.
     *
     * <p>Пример: {@code "Software Developer"}, {@code "HR Manager"}, {@code "Chief Executive Officer"}
     */
    @Column(name = "position", length = 100)
    private String position;

    /**
     * Отдел или подразделение, в котором работает сотрудник.
     * Максимальная длина: 100 символов.
     *
     * <p>Пример: {@code "IT Department"}, {@code "Human Resources"}, {@code "Sales Division"}
     */
    @Column(name = "department", length = 100)
    private String department;

    /**
     * График работы сотрудника.
     * Определяет режим рабочего времени.
     *
     * <p>Хранится в базе данных как строка:
     * <ul>
     *   <li>{@code "FULL_TIME"} - полный рабочий день</li>
     *   <li>{@code "PART_TIME"} - неполный рабочий день</li>
     *   <li>{@code "REMOTE"} - удаленная работа</li>
     *   <li>{@code "FLEXIBLE"} - гибкий график</li>
     *   <li>{@code "SHIFT"} - работа в сменах</li>
     * </ul>
     *
     * @see WorkSchedule
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "work_schedule", length = 20)
    private WorkSchedule workSchedule;

    /**
     * Заработная плата сотрудника.
     * Указывается в валюте, определенной в поле {@link #currency}.
     *
     * <p>Пример: {@code 75000.0} (75,000.00 в указанной валюте)
     */
    @Column(name = "salary")
    private Double salary;

    /**
     * Валюта заработной платы.
     * Используется международный код валюты (ISO 4217).
     * Максимальная длина: 3 символа.
     *
     * <p>Пример: {@code "RUB"}, {@code "USD"}, {@code "EUR"}
     */
    @Column(name = "currency", length = 3)
    private String currency;

    /**
     * Тип занятости сотрудника.
     * Определяет характер трудовых отношений.
     *
     * <p>Хранится в базе данных как строка:
     * <ul>
     *   <li>{@code "FULL_TIME"} - штатный сотрудник</li>
     *   <li>{@code "PART_TIME"} - совместитель</li>
     *   <li>{@code "CONTRACT"} - по контракту</li>
     *   <li>{@code "INTERN"} - стажер</li>
     *   <li>{@code "TEMPORARY"} - временный сотрудник</li>
     * </ul>
     *
     * @see EmploymentType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 20)
    private EmploymentType employmentType;

    /**
     * Рабочий email сотрудника.
     * Используется для служебной переписки.
     * Максимальная длина: 100 символов.
     *
     * <p>Может отличаться от личного email, указанного в родительском классе.
     */
    @Column(name = "work_email", length = 100)
    private String workEmail;

    /**
     * Рабочий телефонный номер сотрудника.
     * Максимальная длина: 20 символов.
     *
     * <p>Используется для служебных контактов внутри организации.
     */
    @Column(name = "work_phone", length = 20)
    private String workPhone;

    /**
     * Местоположение офиса, где работает сотрудник.
     * Максимальная длина: 100 символов.
     *
     * <p>Пример: {@code "Moscow Office"}, {@code "New York Branch"}, {@code "Remote"}
     */
    @Column(name = "office_location", length = 100)
    private String officeLocation;

    /**
     * Идентификатор непосредственного руководителя сотрудника.
     * Ссылается на {@link #employeeId} руководителя.
     *
     * <p>Используется для построения организационной структуры и иерархии подчинения.
     */
    @Column(name = "supervisor_id")
    private Long supervisorId;

    /**
     * Статус активности сотрудника.
     * {@code true} - сотрудник действующий, {@code false} - уволен.
     *
     * <p>По умолчанию {@code true}. Для уволенных сотрудников рекомендуется
     * также заполнять поле {@link #terminationDate}.
     */
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Имя контактного лица для экстренных ситуаций.
     * Максимальная длина: 100 символов.
     *
     * <p>Используется в случае необходимости экстренной связи с родственниками
     * или близкими сотрудником.
     */
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    /**
     * Телефон контактного лица для экстренных ситуаций.
     * Максимальная длина: 20 символов.
     *
     * <p>Должен быть актуальным номером для быстрой связи в чрезвычайных ситуациях.
     */
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    /**
     * Номер банковского счета сотрудника.
     * Используется для перечисления заработной платы.
     * Максимальная длина: 50 символов.
     *
     * <p>Формат зависит от страны и банковской системы.
     */
    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    /**
     * Название банка, в котором открыт счет сотрудника.
     * Максимальная длина: 100 символов.
     *
     * <p>Используется для идентификации финансового учреждения при расчетах.
     */
    @Column(name = "bank_name", length = 100)
    private String bankName;

    /**
     * Проверяет, является ли сотрудник действующим.
     *
     * @return {@code true} если сотрудник активен, {@code false} если уволен
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Проверяет, уволен ли сотрудник.
     *
     * @return {@code true} если сотрудник уволен, {@code false} если действующий
     */
    public boolean isTerminated() {
        return !isActive() || terminationDate != null;
    }

    /**
     * Получает продолжительность работы в днях.
     *
     * @return количество дней работы, или 0 если дата приема не указана
     */
    public long getDaysEmployed() {
        if (hireDate == null) {
            return 0;
        }
        LocalDate endDate = (terminationDate != null) ? terminationDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(hireDate, endDate);
    }
}