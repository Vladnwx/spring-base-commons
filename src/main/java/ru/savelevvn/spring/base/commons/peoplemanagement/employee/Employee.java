package ru.savelevvn.spring.base.commons.peoplemanagement.employee;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
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
 * @version 1.0
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
@ToString(callSuper = true, of = {"employeeId", "position", "department"})
@EqualsAndHashCode(callSuper = true, of = {"employeeId"})
public abstract class Employee extends Person {

    /**
     * Уникальный идентификатор сотрудника в организации.
     * Используется для внутренней идентификации работника.
     * Максимальная длина: 20 символов.
     *
     * <p>Пример: {@code "EMP001234"}, {@code "DEV567890"}
     */
    @Column(name = "employee_id", unique = true, length = 20)
    @Pattern(regexp = "^[A-Z0-9_\\-]{1,20}$", message = "Некорректный формат ID сотрудника")
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
    @DecimalMin(value = "0.0", message = "Заработная плата не может быть отрицательной")
    private Double salary;

    /**
     * Валюта заработной платы.
     * Используется международный код валюты (ISO 4217).
     * Максимальная длина: 3 символа.
     *
     * <p>Пример: {@code "RUB"}, {@code "USD"}, {@code "EUR"}
     */
    @Column(name = "currency", length = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "Некорректный код валюты")
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
     * Предобработка сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     */
    @Override
    protected void prePersist() {
        super.prePersist();

        // Нормализация данных сотрудника
        if (this.employeeId != null) {
            this.employeeId = this.employeeId.toUpperCase().trim();
        }
        if (this.position != null) {
            this.position = this.position.trim();
        }
        if (this.department != null) {
            this.department = this.department.trim();
        }
        if (this.workEmail != null) {
            this.workEmail = this.workEmail.toLowerCase().trim();
        }
        if (this.currency != null) {
            this.currency = this.currency.toUpperCase().trim();
        }
    }

    /**
     * Валидация сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @throws IllegalArgumentException если сущность не прошла валидацию
     */
    @Override
    protected void validate() {
        super.validate();

        // Проверка корректности дат
        if (hireDate != null && hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата приема на работу не может быть в будущем");
        }

        if (terminationDate != null && hireDate != null && terminationDate.isBefore(hireDate)) {
            throw new IllegalArgumentException("Дата увольнения не может быть раньше даты приема на работу");
        }

        // Проверка валюты
        if (salary != null && currency == null) {
            throw new IllegalArgumentException("При указании зарплаты необходимо указать валюту");
        }

        // Проверка активности
        if (Boolean.FALSE.equals(isActive) && terminationDate == null) {
            throw new IllegalArgumentException("Для неактивного сотрудника необходимо указать дату увольнения");
        }
    }

    /**
     * Проверяет, является ли сотрудник действующим.
     *
     * @return {@code true} если сотрудник активен, {@code false} если уволен
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive) && terminationDate == null;
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

    /**
     * Получает продолжительность работы в годах.
     *
     * @return количество лет работы, или 0 если дата приема не указана
     */
    public int getYearsEmployed() {
        if (hireDate == null) {
            return 0;
        }
        LocalDate endDate = (terminationDate != null) ? terminationDate : LocalDate.now();
        return (int) java.time.temporal.ChronoUnit.YEARS.between(hireDate, endDate);
    }

    /**
     * Проверяет, является ли сотрудник новичком (менее 6 месяцев работы).
     *
     * @return {@code true} если сотрудник работает менее 6 месяцев
     */
    public boolean isRookie() {
        return getDaysEmployed() < 180;
    }

    /**
     * Проверяет, является ли сотрудник ветераном (более 5 лет работы).
     *
     * @return {@code true} если сотрудник работает более 5 лет
     */
    public boolean isVeteran() {
        return getYearsEmployed() >= 5;
    }

    /**
     * Получает стаж работы в формате "X лет Y месяцев".
     *
     * @return строковое представление стажа работы
     */
    public String getEmploymentPeriod() {
        if (hireDate == null) {
            return "Не определено";
        }

        LocalDate endDate = (terminationDate != null) ? terminationDate : LocalDate.now();
        long totalMonths = java.time.temporal.ChronoUnit.MONTHS.between(hireDate, endDate);

        long years = totalMonths / 12;
        long months = totalMonths % 12;

        StringBuilder result = new StringBuilder();
        if (years > 0) {
            result.append(years).append(" ").append(getYearForm(years));
        }
        if (months > 0) {
            if (years > 0) result.append(" ");
            result.append(months).append(" ").append(getMonthForm(months));
        }

        return !result.isEmpty() ? result.toString() : "Меньше месяца";
    }

    /**
     * Проверяет, имеет ли сотрудник руководителя.
     *
     * @return {@code true} если у сотрудника есть руководитель
     */
    public boolean hasSupervisor() {
        return supervisorId != null;
    }

    /**
     * Проверяет, является ли сотрудник руководителем.
     *
     * @return {@code true} если у сотрудника есть подчиненные
     */
    public boolean isSupervisor() {
        // Эта проверка должна быть реализована в сервисе
        return false;
    }

    /**
     * Проверяет, является ли сотрудник удаленным.
     *
     * @return {@code true} если сотрудник работает удаленно
     */
    public boolean isRemote() {
        return workSchedule == WorkSchedule.REMOTE;
    }

    /**
     * Получает форматированную заработную плату.
     *
     * @return строковое представление заработной платы
     */
    public String getFormattedSalary() {
        if (salary == null || currency == null) {
            return "Не указана";
        }
        return String.format("%.2f %s", salary, currency);
    }

    /**
     * Проверяет, совпадает ли рабочий email с личным.
     *
     * @return {@code true} если рабочий и личный email совпадают
     */
    public boolean isWorkEmailSameAsPersonal() {
        return getEmail() != null && workEmail != null && getEmail().equalsIgnoreCase(workEmail);
    }

    /**
     * Получает форму слова "год" в зависимости от числа.
     */
    private String getYearForm(long years) {
        long lastDigit = years % 10;
        long lastTwoDigits = years % 100;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
            return "лет";
        } else if (lastDigit == 1) {
            return "год";
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return "года";
        } else {
            return "лет";
        }
    }

    /**
     * Получает форму слова "месяц" в зависимости от числа.
     */
    private String getMonthForm(long months) {
        long lastDigit = months % 10;
        long lastTwoDigits = months % 100;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
            return "месяцев";
        } else if (lastDigit == 1) {
            return "месяц";
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return "месяца";
        } else {
            return "месяцев";
        }
    }
}