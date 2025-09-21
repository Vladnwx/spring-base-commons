package ru.savelevvn.spring.base.commons.peoplemanagement.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.Employee;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.EmploymentType;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.WorkSchedule;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.repository.EmployeeRepository;
import ru.savelevvn.spring.base.commons.peoplemanagement.service.PersonService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Абстрактный базовый сервис для работы с сотрудниками.
 * Расширяет PersonService, добавляя бизнес-логику, специфичную для сотрудников.
 *
 * <p>Основные функции сервиса:
 * <ul>
 *   <li>Управление информацией о сотрудниках</li>
 *   <li>Работа с трудовыми отношениями (прием, увольнение)</li>
 *   <li>Управление заработной платой и льготами</li>
 *   <li>Организационная структура (руководители, подчиненные)</li>
 *   <li>Рабочие характеристики (график, тип занятости)</li>
 * </ul>
 *
 * <p>Сервис обеспечивает соблюдение бизнес-правил при работе с сотрудниками
 * и поддерживает целостность организационной структуры.
 *
 * @param <T> тип сущности, расширяющей Employee
 * @param <R> тип репозитория, расширяющего EmployeeRepository
 * @version 1.0
 * @see Employee
 * @see EmployeeRepository
 * @see PersonService
 */
@Slf4j
@Transactional(readOnly = true)
public abstract class EmployeeService<T extends Employee, R extends EmployeeRepository<T>>
        extends PersonService<T, R> {

    // Паттерны для валидации
    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("^[A-Z0-9_\\-]{3,20}$");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");

    /**
     * Конструктор сервиса.
     *
     * @param repository репозиторий для работы с сотрудниками
     */
    protected EmployeeService(R repository) {
        super(repository);
    }

    /**
     * Предобработка сотрудника перед сохранением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     *
     * @param employee сотрудник для предобработки
     * @return обработанный сотрудник
     */
    @Override
    protected T preSave(T employee) {
        log.trace("Предобработка сотрудника перед сохранением: {}", employee);

        // Нормализация данных сотрудника
        if (employee.getEmployeeId() != null) {
            employee.setEmployeeId(employee.getEmployeeId().toUpperCase().trim());
        }
        if (employee.getPosition() != null) {
            employee.setPosition(employee.getPosition().trim());
        }
        if (employee.getDepartment() != null) {
            employee.setDepartment(employee.getDepartment().trim());
        }
        if (employee.getWorkEmail() != null) {
            employee.setWorkEmail(employee.getWorkEmail().toLowerCase().trim());
        }
        if (employee.getCurrency() != null) {
            employee.setCurrency(employee.getCurrency().toUpperCase().trim());
        }

        return employee;
    }

    /**
     * Валидация сотрудника перед сохранением.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @param employee сотрудник для валидации
     * @throws IllegalArgumentException если сотрудник не прошел валидацию
     */
    @Override
    protected void validate(T employee) {
        log.trace("Валидация сотрудника: {}", employee);
        validateEmployee(employee);
    }

    /**
     * Постобработка сотрудника после сохранения.
     * Может быть переопределена в подклассах для реализации дополнительной логики.
     *
     * @param employee сохраненный сотрудник
     * @return обработанный сотрудник
     */
    @Override
    protected T postSave(T employee) {
        log.trace("Постобработка сотрудника после сохранения: {}", employee);
        return employee;
    }

    /**
     * Создает нового сотрудника.
     *
     * @param employee сотрудник для создания
     * @return созданный сотрудник
     * @throws IllegalArgumentException если сотрудник не прошел валидацию
     */
    @Transactional
    public T createEmployee(T employee) {
        log.debug("Создание нового сотрудника: {}", employee);

        try {
            validateEmployee(employee);

            // Генерация табельного номера, если не указан
            if (employee.getEmployeeId() == null || employee.getEmployeeId().trim().isEmpty()) {
                employee.setEmployeeId(generateEmployeeId());
                log.info("Сгенерирован табельный номер для сотрудника: {}", employee.getEmployeeId());
            }

            T createdEmployee = save(employee);
            log.info("Сотрудник успешно создан с ID: {} и табельным номером: {}",
                    createdEmployee.getId(), createdEmployee.getEmployeeId());

            return createdEmployee;
        } catch (Exception e) {
            log.error("Ошибка при создании сотрудника: ", e);
            throw e;
        }
    }

    /**
     * Обновляет информацию о существующем сотруднике.
     *
     * @param id идентификатор сотрудника
     * @param employee обновленная информация о сотруднике
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден или не прошел валидацию
     */
    @Transactional
    public T updateEmployee(Long id, T employee) {
        log.debug("Обновление сотрудника с ID {}: {}", id, employee);

        try {
            // Проверяем существование сотрудника
            T existingEmployee = findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + id + " не найден"));

            // Устанавливаем ID для обновления
            employee.setId(id);

            // Сохраняем табельный номер (не должен изменяться)
            employee.setEmployeeId(existingEmployee.getEmployeeId());

            validateEmployee(employee);
            T updatedEmployee = save(employee);

            log.info("Сотрудник успешно обновлен с ID: {}", id);
            return updatedEmployee;
        } catch (Exception e) {
            log.error("Ошибка при обновлении сотрудника с ID {}: ", id, e);
            throw e;
        }
    }

    /**
     * Увольняет сотрудника.
     *
     * @param id идентификатор сотрудника
     * @param terminationDate дата увольнения
     * @param reason причина увольнения
     * @return уволенный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден или уже уволен
     */
    @Transactional
    public T terminateEmployee(Long id, LocalDate terminationDate, String reason) {
        log.debug("Увольнение сотрудника с ID {}: дата={}, причина={}", id, terminationDate, reason);

        try {
            T employee = findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + id + " не найден"));

            if (employee.getTerminationDate() != null) {
                throw new IllegalArgumentException("Сотрудник уже уволен");
            }

            employee.setTerminationDate(terminationDate);
            employee.setTerminationReason(reason);
            employee.setIsActive(false);

            T terminatedEmployee = save(employee);
            log.info("Сотрудник успешно уволен с ID: {}", id);

            return terminatedEmployee;
        } catch (Exception e) {
            log.error("Ошибка при увольнении сотрудника с ID {}: ", id, e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников, у которых срок работы превышает указанное количество лет.
     *
     * @param years количество лет
     * @param pageable параметры пагинации
     * @return страница с сотрудниками со стажем более указанного количества лет
     */
    public Page<T> findEmployeesWithExperienceMoreThanYears(int years, Pageable pageable) {
        log.debug("Поиск сотрудников со стажем более {} лет", years);

        try {
            LocalDate minDate = LocalDate.now().minusYears(years);
            Page<T> employees = getRepository().findEmployeesWithExperienceMoreThanYears(minDate, pageable);

            log.trace("Найдено {} сотрудников со стажем более {} лет", employees.getNumberOfElements(), years);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников со стажем более {} лет: ", years, e);
            throw e;
        }
    }

    /**
     * Активирует (восстанавливает) сотрудника.
     *
     * @param id идентификатор сотрудника
     * @return активированный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден или уже активен
     */
    @Transactional
    public T activateEmployee(Long id) {
        log.debug("Активация сотрудника с ID: {}", id);

        try {
            T employee = findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + id + " не найден"));

            if (employee.getTerminationDate() == null) {
                throw new IllegalArgumentException("Сотрудник уже активен");
            }

            employee.setTerminationDate(null);
            employee.setTerminationReason(null);
            employee.setIsActive(true);

            T activatedEmployee = save(employee);
            log.info("Сотрудник успешно активирован с ID: {}", id);

            return activatedEmployee;
        } catch (Exception e) {
            log.error("Ошибка при активации сотрудника с ID {}: ", id, e);
            throw e;
        }
    }

    /**
     * Находит сотрудника по табельному номеру.
     *
     * @param employeeId табельный номер сотрудника
     * @return Optional с найденным сотрудником или пустой Optional
     */
    public Optional<T> findByEmployeeId(String employeeId) {
        log.debug("Поиск сотрудника по табельному номеру: {}", employeeId);

        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.warn("Попытка поиска по пустому табельному номеру");
                return Optional.empty();
            }

            Optional<T> employee = getRepository().findByEmployeeId(employeeId.trim());
            log.trace("Найден {} сотрудник по табельному номеру: {}", employee.isPresent() ? 1 : 0, employeeId);
            return employee;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудника по табельному номеру {}: ", employeeId, e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников по указанной должности.
     *
     * @param position должность для поиска
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанной должностью
     */
    public Page<T> findByPosition(String position, Pageable pageable) {
        log.debug("Поиск сотрудников по должности: {}", position);

        try {
            if (position == null || position.trim().isEmpty()) {
                log.warn("Попытка поиска по пустой должности");
                return Page.empty();
            }

            Page<T> employees = getRepository().findByPosition(position.trim(), pageable);
            log.trace("Найдено {} сотрудников по должности: {}", employees.getNumberOfElements(), position);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по должности {}: ", position, e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников по указанному отделу.
     *
     * @param department отдел для поиска
     * @param pageable параметры пагинации
     * @return страница с сотрудниками из указанного отдела
     */
    public Page<T> findByDepartment(String department, Pageable pageable) {
        log.debug("Поиск сотрудников по отделу: {}", department);

        try {
            if (department == null || department.trim().isEmpty()) {
                log.warn("Попытка поиска по пустому отделу");
                return Page.empty();
            }

            Page<T> employees = getRepository().findByDepartment(department.trim(), pageable);
            log.trace("Найдено {} сотрудников по отделу: {}", employees.getNumberOfElements(), department);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по отделу {}: ", department, e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников, принятых на работу в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, принятыми в указанный период
     */
    public Page<T> findByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Поиск сотрудников по дате приема между: {} и {}", startDate, endDate);

        try {
            if (startDate == null || endDate == null) {
                log.warn("Попытка поиска по null датам");
                return Page.empty();
            }

            Page<T> employees = getRepository().findByHireDateBetween(startDate, endDate, pageable);
            log.trace("Найдено {} сотрудников по дате приема между: {} и {}",
                    employees.getNumberOfElements(), startDate, endDate);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по дате приема между {} и {}: ", startDate, endDate, e);
            throw e;
        }
    }

    /**
     * Находит всех активных сотрудников.
     *
     * @param pageable параметры пагинации
     * @return страница с активными сотрудниками
     */
    public Page<T> findActiveEmployees(Pageable pageable) {
        log.debug("Поиск активных сотрудников");

        try {
            Page<T> employees = getRepository().findByTerminationDateIsNull(pageable);
            log.trace("Найдено {} активных сотрудников", employees.getNumberOfElements());
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске активных сотрудников: ", e);
            throw e;
        }
    }

    /**
     * Находит всех неактивных (уволенных) сотрудников.
     *
     * @param pageable параметры пагинации
     * @return страница с неактивными сотрудниками
     */
    public Page<T> findInactiveEmployees(Pageable pageable) {
        log.debug("Поиск неактивных сотрудников");

        try {
            Page<T> employees = getRepository().findByTerminationDateIsNotNull(pageable);
            log.trace("Найдено {} неактивных сотрудников", employees.getNumberOfElements());
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске неактивных сотрудников: ", e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников с указанным типом занятости.
     *
     * @param employmentType тип занятости
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанным типом занятости
     */
    public Page<T> findByEmploymentType(EmploymentType employmentType, Pageable pageable) {
        log.debug("Поиск сотрудников по типу занятости: {}", employmentType);

        try {
            if (employmentType == null) {
                log.warn("Попытка поиска по null типу занятости");
                return Page.empty();
            }

            Page<T> employees = getRepository().findByEmploymentType(employmentType, pageable);
            log.trace("Найдено {} сотрудников по типу занятости: {}", employees.getNumberOfElements(), employmentType);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по типу занятости {}: ", employmentType, e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников с указанным графиком работы.
     *
     * @param workSchedule график работы
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанным графиком работы
     */
    public Page<T> findByWorkSchedule(WorkSchedule workSchedule, Pageable pageable) {
        log.debug("Поиск сотрудников по графику работы: {}", workSchedule);

        try {
            if (workSchedule == null) {
                log.warn("Попытка поиска по null графику работы");
                return Page.empty();
            }

            Page<T> employees = getRepository().findByWorkSchedule(workSchedule, pageable);
            log.trace("Найдено {} сотрудников по графику работы: {}", employees.getNumberOfElements(), workSchedule);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по графику работы {}: ", workSchedule, e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников с заработной платой в указанном диапазоне.
     *
     * @param minSalary минимальная заработная плата
     * @param maxSalary максимальная заработная плата
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с заработной платой в указанном диапазоне
     */
    public Page<T> findBySalaryBetween(Double minSalary, Double maxSalary, Pageable pageable) {
        log.debug("Поиск сотрудников по зарплате между: {} и {}", minSalary, maxSalary);

        try {
            if (minSalary == null || maxSalary == null) {
                log.warn("Попытка поиска по null значениям зарплаты");
                return Page.empty();
            }

            Page<T> employees = getRepository().findBySalaryBetween(minSalary, maxSalary, pageable);
            log.trace("Найдено {} сотрудников по зарплате между: {} и {}",
                    employees.getNumberOfElements(), minSalary, maxSalary);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по зарплате между {} и {}: ", minSalary, maxSalary, e);
            throw e;
        }
    }

    /**
     * Находит всех сотрудников с указанным руководителем.
     *
     * @param supervisorId идентификатор руководителя
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанным руководителем
     */
    public Page<T> findBySupervisorId(Long supervisorId, Pageable pageable) {
        log.debug("Поиск сотрудников по руководителю с ID: {}", supervisorId);

        try {
            if (supervisorId == null) {
                log.warn("Попытка поиска по null ID руководителя");
                return Page.empty();
            }

            Page<T> employees = getRepository().findBySupervisorId(supervisorId, pageable);
            log.trace("Найдено {} сотрудников по руководителю с ID: {}", employees.getNumberOfElements(), supervisorId);
            return employees;
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по руководителю с ID {}: ", supervisorId, e);
            throw e;
        }
    }

    /**
     * Назначает руководителя сотруднику.
     *
     * @param employeeId идентификатор сотрудника
     * @param supervisorId идентификатор руководителя
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник или руководитель не найдены
     */
    @Transactional
    public T assignSupervisor(Long employeeId, Long supervisorId) {
        log.debug("Назначение руководителя {} сотруднику {}", supervisorId, employeeId);

        try {
            T employee = findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

            // Проверяем, что руководитель существует
            findById(supervisorId)
                    .orElseThrow(() -> new IllegalArgumentException("Руководитель с ID " + supervisorId + " не найден"));

            employee.setSupervisorId(supervisorId);
            T updatedEmployee = save(employee);

            log.info("Руководитель {} успешно назначен сотруднику {}", supervisorId, employeeId);
            return updatedEmployee;
        } catch (Exception e) {
            log.error("Ошибка при назначении руководителя {} сотруднику {}: ", supervisorId, employeeId, e);
            throw e;
        }
    }

    /**
     * Удаляет руководителя у сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден
     */
    @Transactional
    public T removeSupervisor(Long employeeId) {
        log.debug("Удаление руководителя у сотрудника с ID: {}", employeeId);

        try {
            T employee = findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

            employee.setSupervisorId(null);
            T updatedEmployee = save(employee);

            log.info("Руководитель успешно удален у сотрудника с ID: {}", employeeId);
            return updatedEmployee;
        } catch (Exception e) {
            log.error("Ошибка при удалении руководителя у сотрудника с ID {}: ", employeeId, e);
            throw e;
        }
    }

    /**
     * Обновляет заработную плату сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @param newSalary новая заработная плата
     * @param currency валюта заработной платы
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден или данные некорректны
     */
    @Transactional
    public T updateSalary(Long employeeId, Double newSalary, String currency) {
        log.debug("Обновление зарплаты сотрудника с ID {}: {} {}", employeeId, newSalary, currency);

        try {
            T employee = findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

            if (!isValidSalary(newSalary)) {
                throw new IllegalArgumentException("Некорректное значение заработной платы: " + newSalary);
            }

            if (!isValidCurrency(currency)) {
                throw new IllegalArgumentException("Некорректная валюта: " + currency);
            }

            employee.setSalary(newSalary);
            employee.setCurrency(currency);
            T updatedEmployee = save(employee);

            log.info("Зарплата успешно обновлена для сотрудника с ID: {}", employeeId);
            return updatedEmployee;
        } catch (Exception e) {
            log.error("Ошибка при обновлении зарплаты сотрудника с ID {}: ", employeeId, e);
            throw e;
        }
    }

    /**
     * Получает количество активных сотрудников.
     *
     * @return количество активных сотрудников
     */
    public long countActiveEmployees() {
        log.debug("Подсчет активных сотрудников");

        try {
            long count = getRepository().countByTerminationDateIsNull();
            log.trace("Найдено {} активных сотрудников", count);
            return count;
        } catch (Exception e) {
            log.error("Ошибка при подсчете активных сотрудников: ", e);
            throw e;
        }
    }

    /**
     * Получает стаж работы сотрудника в годах.
     *
     * @param employeeId идентификатор сотрудника
     * @return стаж работы в годах
     * @throws IllegalArgumentException если сотрудник не найден
     */
    public int getYearsOfService(Long employeeId) {
        log.debug("Получение стажа работы сотрудника с ID: {}", employeeId);

        try {
            T employee = findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

            if (employee.getHireDate() == null) {
                return 0;
            }

            LocalDate now = LocalDate.now();
            int years = Period.between(employee.getHireDate(), now).getYears();
            log.trace("Стаж работы сотрудника с ID {} составляет {} лет", employeeId, years);
            return years;
        } catch (Exception e) {
            log.error("Ошибка при получении стажа работы сотрудника с ID {}: ", employeeId, e);
            throw e;
        }
    }

    /**
     * Проверяет, является ли сотрудник руководителем.
     *
     * @param employeeId идентификатор сотрудника
     * @return true если сотрудник является руководителем, false в противном случае
     */
    public boolean isSupervisor(Long employeeId) {
        log.debug("Проверка, является ли сотрудник с ID {} руководителем", employeeId);

        try {
            long count = getRepository().countBySupervisorId(employeeId);
            boolean isSupervisor = count > 0;
            log.trace("Сотрудник с ID {} {} является руководителем", employeeId, isSupervisor ? "" : "не");
            return isSupervisor;
        } catch (Exception e) {
            log.error("Ошибка при проверке, является ли сотрудник с ID {} руководителем: ", employeeId, e);
            throw e;
        }
    }

    /**
     * Валидирует сотрудника перед сохранением.
     *
     * @param employee сотрудник для валидации
     * @throws IllegalArgumentException если сотрудник не прошел валидацию
     */
    public void validateEmployee(T employee) {
        log.trace("Валидация сотрудника: {}", employee);

        if (employee == null) {
            throw new IllegalArgumentException("Сотрудник не может быть null");
        }

        // Базовая валидация Person
        validatePerson(employee);

        // Дополнительная валидация для Employee
        if (employee.getHireDate() == null) {
            throw new IllegalArgumentException("Дата приема на работу обязательна");
        }

        if (employee.getHireDate() != null && employee.getHireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата приема на работу не может быть в будущем");
        }

        if (employee.getPosition() == null || employee.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Должность обязательна");
        }

        if (employee.getDepartment() == null || employee.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Отдел обязателен");
        }

        if (employee.getEmployeeId() != null && !employee.getEmployeeId().trim().isEmpty() &&
                !isValidEmployeeId(employee.getEmployeeId())) {
            throw new IllegalArgumentException("Некорректный формат табельного номера: " + employee.getEmployeeId());
        }

        if (employee.getSalary() != null && !isValidSalary(employee.getSalary())) {
            throw new IllegalArgumentException("Некорректное значение заработной платы: " + employee.getSalary());
        }

        if (employee.getCurrency() != null && !employee.getCurrency().trim().isEmpty() &&
                !isValidCurrency(employee.getCurrency())) {
            throw new IllegalArgumentException("Некорректная валюта: " + employee.getCurrency());
        }

        // Проверка корректности дат увольнения
        if (employee.getTerminationDate() != null && employee.getHireDate() != null &&
                employee.getTerminationDate().isBefore(employee.getHireDate())) {
            throw new IllegalArgumentException("Дата увольнения не может быть раньше даты приема на работу");
        }

        // Проверка уникальности табельного номера
        if (employee.getEmployeeId() != null && !employee.getEmployeeId().trim().isEmpty()) {
            Optional<T> existing = findByEmployeeId(employee.getEmployeeId());
            if (existing.isPresent() && !existing.get().getId().equals(employee.getId())) {
                throw new IllegalArgumentException("Сотрудник с таким табельным номером уже существует: " + employee.getEmployeeId());
            }
        }

        // Проверка уникальности рабочего email
        if (employee.getWorkEmail() != null && !employee.getWorkEmail().trim().isEmpty()) {
            List<T> existing = getRepository().findByWorkEmail(employee.getWorkEmail());
            if (!existing.isEmpty() && !existing.get(0).getId().equals(employee.getId())) {
                throw new IllegalArgumentException("Сотрудник с таким рабочим email уже существует: " + employee.getWorkEmail());
            }
        }
    }

    /**
     * Проверяет валидность табельного номера сотрудника.
     *
     * @param employeeId табельный номер для проверки
     * @return true если табельный номер валидный, false в противном случае
     */
    public boolean isValidEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) return false;
        boolean valid = EMPLOYEE_ID_PATTERN.matcher(employeeId.trim()).matches();
        log.trace("Табельный номер {} валидность: {}", employeeId, valid);
        return valid;
    }

    /**
     * Проверяет валидность заработной платы.
     *
     * @param salary заработная плата для проверки
     * @return true если заработная плата валидная, false в противном случае
     */
    public boolean isValidSalary(Double salary) {
        if (salary == null) return true;
        boolean valid = salary >= 0;
        log.trace("Зарплата {} валидность: {}", salary, valid);
        return valid;
    }

    /**
     * Проверяет валидность валюты.
     *
     * @param currency валюта для проверки
     * @return true если валюта валидная, false в противном случае
     */
    public boolean isValidCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) return true;
        boolean valid = CURRENCY_PATTERN.matcher(currency.trim()).matches();
        log.trace("Валюта {} валидность: {}", currency, valid);
        return valid;
    }

    /**
     * Генерирует уникальный табельный номер для нового сотрудника.
     *
     * @return сгенерированный табельный номер
     */
    public String generateEmployeeId() {
        log.debug("Генерация уникального табельного номера");

        // Генерация уникального табельного номера
        String employeeId;
        int attempts = 0;
        do {
            employeeId = "EMP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Не удалось сгенерировать уникальный табельный номер после 100 попыток");
            }
        } while (existsByEmployeeId(employeeId));

        log.trace("Сгенерирован табельный номер: {}", employeeId);
        return employeeId;
    }

    /**
     * Проверяет существование сотрудника с указанным табельным номером.
     *
     * @param employeeId табельный номер для проверки
     * @return true если сотрудник с таким табельным номером существует, false в противном случае
     */
    public boolean existsByEmployeeId(String employeeId) {
        log.debug("Проверка существования сотрудника по табельному номеру: {}", employeeId);

        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return false;
            }
            boolean exists = getRepository().existsByEmployeeId(employeeId.trim());
            log.trace("Сотрудник с табельным номером {} {}", employeeId, exists ? "существует" : "не существует");
            return exists;
        } catch (Exception e) {
            log.error("Ошибка при проверке существования сотрудника по табельному номеру {}: ", employeeId, e);
            throw e;
        }
    }

    /**
     * Создание страницы из списка.
     */
    protected Page<T> createPageFromList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<T> pageContent = start <= list.size() ? list.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, list.size());
    }
}