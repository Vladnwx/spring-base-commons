package ru.savelevvn.spring.base.commons.peoplemanagement.employee.service;

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
 *
 * @since 1.0
 * @see Employee
 * @see EmployeeRepository
 * @see PersonService
 */
@Transactional
public abstract class EmployeeService<T extends Employee, R extends EmployeeRepository<T>>
        extends PersonService<T, R> {

    // Паттерны для валидации
    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("^[A-Z0-9]{3,20}$");
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
     * Создает нового сотрудника.
     *
     * @param employee сотрудник для создания
     * @return созданный сотрудник
     * @throws IllegalArgumentException если сотрудник не прошел валидацию
     */
    public T createEmployee(T employee) {
        validateEmployee(employee);

        // Генерация табельного номера, если не указан
        if (employee.getEmployeeId() == null) {
            employee.setEmployeeId(generateEmployeeId());
        }

        return save(employee);
    }

    /**
     * Обновляет информацию о существующем сотруднике.
     *
     * @param id идентификатор сотрудника
     * @param employee обновленная информация о сотруднике
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден или не прошел валидацию
     */
    public T updateEmployee(Long id, T employee) {
        // Проверяем существование сотрудника
        T existingEmployee = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + id + " не найден"));

        // Устанавливаем ID для обновления
        employee.setId(id);

        // Сохраняем табельный номер (не должен изменяться)
        employee.setEmployeeId(existingEmployee.getEmployeeId());

        validateEmployee(employee);
        return save(employee);
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
    public T terminateEmployee(Long id, LocalDate terminationDate, String reason) {
        T employee = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + id + " не найден"));

        if (employee.getTerminationDate() != null) {
            throw new IllegalArgumentException("Сотрудник уже уволен");
        }

        employee.setTerminationDate(terminationDate);
        employee.setTerminationReason(reason);
        employee.setIsActive(false);

        return save(employee);
    }

    /**
     * Находит всех сотрудников, у которых срок работы превышает указанное количество лет.
     *
     * @param years количество лет
     * @return список сотрудников со стажем более указанного количества лет
     */
    public List<T> findEmployeesWithExperienceMoreThanYears(int years) {
        LocalDate minDate = LocalDate.now().minusYears(years);
        return getRepository().findEmployeesWithExperienceMoreThanYears(minDate);
    }

    /**
     * Активирует (восстанавливает) сотрудника.
     *
     * @param id идентификатор сотрудника
     * @return активированный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден или уже активен
     */
    public T activateEmployee(Long id) {
        T employee = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + id + " не найден"));

        if (employee.getTerminationDate() == null) {
            throw new IllegalArgumentException("Сотрудник уже активен");
        }

        employee.setTerminationDate(null);
        employee.setTerminationReason(null);
        employee.setIsActive(true);

        return save(employee);
    }

    /**
     * Находит сотрудника по табельному номеру.
     *
     * @param employeeId табельный номер сотрудника
     * @return Optional с найденным сотрудником или пустой Optional
     */
    public Optional<T> findByEmployeeId(String employeeId) {
        return getRepository().findByEmployeeId(employeeId);
    }

    /**
     * Находит всех сотрудников по указанной должности.
     *
     * @param position должность для поиска
     * @return список сотрудников с указанной должностью
     */
    public List<T> findByPosition(String position) {
        return getRepository().findByPosition(position);
    }

    /**
     * Находит всех сотрудников по указанному отделу.
     *
     * @param department отдел для поиска
     * @return список сотрудников из указанного отдела
     */
    public List<T> findByDepartment(String department) {
        return getRepository().findByDepartment(department);
    }

    /**
     * Находит всех сотрудников, принятых на работу в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список сотрудников, принятых в указанный период
     */
    public List<T> findByHireDateBetween(LocalDate startDate, LocalDate endDate) {
        return getRepository().findByHireDateBetween(startDate, endDate);
    }

    /**
     * Находит всех сотрудников, уволенных в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список сотрудников, уволенных в указанный период
     */
    public List<T> findByTerminationDateBetween(LocalDate startDate, LocalDate endDate) {
        return getRepository().findByTerminationDateBetween(startDate, endDate);
    }

    /**
     * Находит всех активных сотрудников.
     *
     * @return список активных сотрудников
     */
    public List<T> findActiveEmployees() {
        return getRepository().findByTerminationDateIsNull();
    }

    /**
     * Находит всех неактивных (уволенных) сотрудников.
     *
     * @return список неактивных сотрудников
     */
    public List<T> findInactiveEmployees() {
        return getRepository().findByTerminationDateIsNotNull();
    }

    /**
     * Находит всех сотрудников с указанным типом занятости.
     *
     * @param employmentType тип занятости
     * @return список сотрудников с указанным типом занятости
     */
    public List<T> findByEmploymentType(EmploymentType employmentType) {
        return getRepository().findByEmploymentType(employmentType);
    }

    /**
     * Находит всех сотрудников с указанным графиком работы.
     *
     * @param workSchedule график работы
     * @return список сотрудников с указанным графиком работы
     */
    public List<T> findByWorkSchedule(WorkSchedule workSchedule) {
        return getRepository().findByWorkSchedule(workSchedule);
    }

    /**
     * Находит всех сотрудников с заработной платой в указанном диапазоне.
     *
     * @param minSalary минимальная заработная плата
     * @param maxSalary максимальная заработная плата
     * @return список сотрудников с заработной платой в указанном диапазоне
     */
    public List<T> findBySalaryBetween(Double minSalary, Double maxSalary) {
        return getRepository().findBySalaryBetween(minSalary, maxSalary);
    }

    /**
     * Находит всех сотрудников с указанной валютой заработной платы.
     *
     * @param currency валюта заработной платы
     * @return список сотрудников с указанной валютой заработной платы
     */
    public List<T> findByCurrency(String currency) {
        return getRepository().findByCurrency(currency);
    }

    /**
     * Находит всех сотрудников с указанным руководителем.
     *
     * @param supervisorId идентификатор руководителя
     * @return список сотрудников с указанным руководителем
     */
    public List<T> findBySupervisorId(Long supervisorId) {
        return getRepository().findBySupervisorId(supervisorId);
    }

    /**
     * Находит всех подчиненных указанного руководителя.
     *
     * @param supervisorId идентификатор руководителя
     * @return список подчиненных сотрудников
     */
    public List<T> findSubordinates(Long supervisorId) {
        return getRepository().findBySupervisorId(supervisorId);
    }

    /**
     * Находит всех сотрудников по рабочему email.
     *
     * @param workEmail рабочий email
     * @return список сотрудников с указанным рабочим email
     */
    public List<T> findByWorkEmail(String workEmail) {
        return getRepository().findByWorkEmail(workEmail);
    }

    /**
     * Находит всех сотрудников по рабочему телефону.
     *
     * @param workPhone рабочий телефон
     * @return список сотрудников с указанным рабочим телефоном
     */
    public List<T> findByWorkPhone(String workPhone) {
        return getRepository().findByWorkPhone(workPhone);
    }

    /**
     * Находит всех сотрудников по местоположению офиса.
     *
     * @param officeLocation местоположение офиса
     * @return список сотрудников по указанному местоположению офиса
     */
    public List<T> findByOfficeLocation(String officeLocation) {
        return getRepository().findByOfficeLocation(officeLocation);
    }

    /**
     * Находит всех сотрудников по части табельного номера.
     *
     * @param employeeId часть табельного номера
     * @return список сотрудников, чей табельный номер содержит указанную строку
     */
    public List<T> findByEmployeeIdContaining(String employeeId) {
        return getRepository().findByEmployeeIdContaining(employeeId);
    }

    /**
     * Находит всех сотрудников по части должности.
     *
     * @param position часть должности
     * @return список сотрудников, чья должность содержит указанную строку
     */
    public List<T> findByPositionContaining(String position) {
        return getRepository().findByPositionContaining(position);
    }

    /**
     * Находит всех сотрудников по части названия отдела.
     *
     * @param department часть названия отдела
     * @return список сотрудников, чей отдел содержит указанную строку
     */
    public List<T> findByDepartmentContaining(String department) {
        return getRepository().findByDepartmentContaining(department);
    }

    /**
     * Находит всех сотрудников, у которых день рождения в указанный месяц.
     *
     * @param month номер месяца (1-12)
     * @return список сотрудников, у которых день рождения в указанный месяц
     */
    public List<T> findEmployeesWithBirthdayInMonth(int month) {
        return getRepository().findByBirthDateMonth(month);
    }

    /**
     * Проверяет существование сотрудника с указанным табельным номером.
     *
     * @param employeeId табельный номер для проверки
     * @return true если сотрудник с таким табельным номером существует, false в противном случае
     */
    public boolean existsByEmployeeId(String employeeId) {
        return getRepository().existsByEmployeeId(employeeId);
    }

    /**
     * Проверяет существование сотрудника с указанным рабочим email.
     *
     * @param workEmail рабочий email для проверки
     * @return true если сотрудник с таким рабочим email существует, false в противном случае
     */
    public boolean existsByWorkEmail(String workEmail) {
        return getRepository().existsByWorkEmail(workEmail);
    }

    /**
     * Назначает руководителя сотруднику.
     *
     * @param employeeId идентификатор сотрудника
     * @param supervisorId идентификатор руководителя
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник или руководитель не найдены
     */
    public T assignSupervisor(Long employeeId, Long supervisorId) {
        T employee = findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

        // Проверяем, что руководитель существует
        findById(supervisorId)
                .orElseThrow(() -> new IllegalArgumentException("Руководитель с ID " + supervisorId + " не найден"));

        employee.setSupervisorId(supervisorId);
        return save(employee);
    }

    /**
     * Удаляет руководителя у сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден
     */
    public T removeSupervisor(Long employeeId) {
        T employee = findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

        employee.setSupervisorId(null);
        return save(employee);
    }

    /**
     * Переводит сотрудника в другой отдел.
     *
     * @param employeeId идентификатор сотрудника
     * @param newDepartment новый отдел
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден
     */
    public T transferToDepartment(Long employeeId, String newDepartment) {
        T employee = findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

        employee.setDepartment(newDepartment);
        return save(employee);
    }

    /**
     * Изменяет должность сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @param newPosition новая должность
     * @return обновленный сотрудник
     * @throws IllegalArgumentException если сотрудник не найден
     */
    public T changePosition(Long employeeId, String newPosition) {
        T employee = findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

        employee.setPosition(newPosition);
        return save(employee);
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
    public T updateSalary(Long employeeId, Double newSalary, String currency) {
        T employee = findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

        if (!isValidSalary(newSalary)) {
            throw new IllegalArgumentException("Некорректное значение заработной платы");
        }

        if (!isValidCurrency(currency)) {
            throw new IllegalArgumentException("Некорректная валюта");
        }

        employee.setSalary(newSalary);
        employee.setCurrency(currency);
        return save(employee);
    }

    /**
     * Получает количество активных сотрудников.
     *
     * @return количество активных сотрудников
     */
    public long countActiveEmployees() {
        return getRepository().countByTerminationDateIsNull();
    }

    /**
     * Получает количество сотрудников по отделам.
     *
     * @return список пар (название отдела, количество сотрудников)
     */
    public List<Object[]> countEmployeesByDepartment() {
        return getRepository().countEmployeesByDepartment();
    }

    /**
     * Получает количество сотрудников по типам занятости.
     *
     * @return список пар (тип занятости, количество сотрудников)
     */
    public List<Object[]> countEmployeesByEmploymentType() {
        return getRepository().countEmployeesByEmploymentType();
    }

    /**
     * Получает среднюю заработную плату сотрудников.
     *
     * @return средняя заработная плата
     */
    public Double getAverageSalary() {
        return getRepository().getAverageSalary();
    }

    /**
     * Получает среднюю заработную плату сотрудников по отделам.
     *
     * @return список пар (название отдела, средняя заработная плата)
     */
    public List<Object[]> getAverageSalaryByDepartment() {
        return getRepository().getAverageSalaryByDepartment();
    }

    /**
     * Получает список сотрудников с наибольшей заработной платой.
     *
     * @param limit максимальное количество сотрудников
     * @return список сотрудников с наибольшей заработной платой
     */
    public List<T> findTopPaidEmployees(int limit) {
        return getRepository().findTopPaidEmployees(limit);
    }

    /**
     * Получает список сотрудников с наименьшей заработной платой.
     *
     * @param limit максимальное количество сотрудников
     * @return список сотрудников с наименьшей заработной платой
     */
    public List<T> findLowestPaidEmployees(int limit) {
        return getRepository().findLowestPaidEmployees(limit);
    }

    /**
     * Получает стаж работы сотрудника в годами.
     *
     * @param employeeId идентификатор сотрудника
     * @return стаж работы в годах
     * @throws IllegalArgumentException если сотрудник не найден
     */
    public int getYearsOfService(Long employeeId) {
        T employee = findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник с ID " + employeeId + " не найден"));

        if (employee.getHireDate() == null) {
            return 0;
        }

        LocalDate now = LocalDate.now();
        return Period.between(employee.getHireDate(), now).getYears();
    }

    /**
     * Проверяет, является ли сотрудник руководителем.
     *
     * @param employeeId идентификатор сотрудника
     * @return true если сотрудник является руководителем, false в противном случае
     */
    public boolean isSupervisor(Long employeeId) {
        return getRepository().countBySupervisorId(employeeId) > 0;
    }

    /**
     * Валидирует сотрудника перед сохранением.
     *
     * @param employee сотрудник для валидации
     * @throws IllegalArgumentException если сотрудник не прошел валидацию
     */
    public void validateEmployee(T employee) {
        // Базовая валидация Person
        validatePerson(employee);

        // Дополнительная валидация для Employee
        if (employee.getHireDate() == null) {
            throw new IllegalArgumentException("Дата приема на работу обязательна");
        }

        if (employee.getPosition() == null || employee.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Должность обязательна");
        }

        if (employee.getDepartment() == null || employee.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Отдел обязателен");
        }

        if (employee.getEmployeeId() != null && !isValidEmployeeId(employee.getEmployeeId())) {
            throw new IllegalArgumentException("Некорректный формат табельного номера");
        }

        if (employee.getSalary() != null && !isValidSalary(employee.getSalary())) {
            throw new IllegalArgumentException("Некорректное значение заработной платы");
        }

        if (employee.getCurrency() != null && !isValidCurrency(employee.getCurrency())) {
            throw new IllegalArgumentException("Некорректная валюта");
        }

        // Проверка уникальности табельного номера
        if (employee.getEmployeeId() != null) {
            Optional<T> existing = findByEmployeeId(employee.getEmployeeId());
            if (existing.isPresent() && !existing.get().getId().equals(employee.getId())) {
                throw new IllegalArgumentException("Сотрудник с таким табельным номером уже существует");
            }
        }

        // Проверка уникальности рабочего email
        if (employee.getWorkEmail() != null) {
            List<T> existing = findByWorkEmail(employee.getWorkEmail());
            if (!existing.isEmpty() && !existing.get(0).getId().equals(employee.getId())) {
                throw new IllegalArgumentException("Сотрудник с таким рабочим email уже существует");
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
        return employeeId != null && EMPLOYEE_ID_PATTERN.matcher(employeeId).matches();
    }

    /**
     * Проверяет валидность заработной платы.
     *
     * @param salary заработная плата для проверки
     * @return true если заработная плата валидная, false в противном случае
     */
    public boolean isValidSalary(Double salary) {
        return salary == null || salary >= 0;
    }

    /**
     * Проверяет валидность валюты.
     *
     * @param currency валюта для проверки
     * @return true если валюта валидная, false в противном случае
     */
    public boolean isValidCurrency(String currency) {
        return currency == null || CURRENCY_PATTERN.matcher(currency).matches();
    }

    /**
     * Генерирует уникальный табельный номер для нового сотрудника.
     *
     * @return сгенерированный табельный номер
     */
    public String generateEmployeeId() {
        // Генерация уникального табельного номера
        String employeeId;
        do {
            employeeId = "EMP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (existsByEmployeeId(employeeId));

        return employeeId;
    }
}