package ru.savelevvn.spring.base.commons.peoplemanagement.employee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.Employee;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.EmploymentType;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.WorkSchedule;
import ru.savelevvn.spring.base.commons.peoplemanagement.repository.PersonRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностями типа Employee.
 * Расширяет PersonRepository, добавляя методы, специфичные для сотрудников.
 *
 * <p>Предоставляет методы для поиска и фильтрации сотрудников по рабочим характеристикам:
 * <ul>
 *   <li>Поиск по табельному номеру</li>
 *   <li>Фильтрация по отделам и должностям</li>
 *   <li>Поиск по датам приема и увольнения</li>
 *   <li>Фильтрация по типу занятости и графику работы</li>
 *   <li>Поиск по руководителям</li>
 * </ul>
 *
 * @param <T> тип сущности, расширяющей Employee
 * @version 1.0
 * @see Employee
 * @see PersonRepository
 */
public interface EmployeeRepository<T extends Employee>
        extends PersonRepository<T> {

    /**
     * Находит сотрудника по табельному номеру.
     *
     * @param employeeId табельный номер сотрудника
     * @return Optional с найденным сотрудником или пустой Optional
     */
    Optional<T> findByEmployeeId(String employeeId);

    /**
     * Находит сотрудника по табельному номеру (включая удаленных).
     *
     * @param employeeId табельный номер сотрудника
     * @return Optional с найденным сотрудником или пустой Optional
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.employeeId = :employeeId")
    Optional<T> findByEmployeeIdWithDeleted(@Param("employeeId") String employeeId);

    /**
     * Находит всех сотрудников по указанной должности.
     *
     * @param position должность для поиска
     * @return список сотрудников с указанной должностью
     */
    List<T> findByPosition(String position);

    /**
     * Находит всех сотрудников по указанной должности с пагинацией.
     *
     * @param position должность для поиска
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанной должностью
     */
    Page<T> findByPosition(String position, Pageable pageable);

    /**
     * Находит всех сотрудников по указанному отделу.
     *
     * @param department отдел для поиска
     * @return список сотрудников из указанного отдела
     */
    List<T> findByDepartment(String department);

    /**
     * Находит всех сотрудников по указанному отделу с пагинацией.
     *
     * @param department отдел для поиска
     * @param pageable параметры пагинации
     * @return страница с сотрудниками из указанного отдела
     */
    Page<T> findByDepartment(String department, Pageable pageable);

    /**
     * Находит всех сотрудников с указанной датой приема на работу.
     *
     * @param hireDate дата приема на работу
     * @return список сотрудников с указанной датой приема
     */
    List<T> findByHireDate(LocalDate hireDate);

    /**
     * Находит всех сотрудников с указанной датой приема на работу с пагинацией.
     *
     * @param hireDate дата приема на работу
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанной датой приема
     */
    Page<T> findByHireDate(LocalDate hireDate, Pageable pageable);

    /**
     * Находит всех сотрудников, принятых на работу в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список сотрудников, принятых в указанный период
     */
    List<T> findByHireDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Находит всех сотрудников, принятых на работу в указанный период с пагинацией.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, принятыми в указанный период
     */
    Page<T> findByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Находит всех сотрудников с указанной датой увольнения.
     *
     * @param terminationDate дата увольнения
     * @return список сотрудников с указанной датой увольнения
     */
    List<T> findByTerminationDate(LocalDate terminationDate);

    /**
     * Находит всех сотрудников, уволенных в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список сотрудников, уволенных в указанный период
     */
    List<T> findByTerminationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Находит всех активных сотрудников (не уволенных).
     *
     * @return список активных сотрудников
     */
    List<T> findByTerminationDateIsNull();

    /**
     * Находит всех активных сотрудников (не уволенных) с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница с активными сотрудниками
     */
    Page<T> findByTerminationDateIsNull(Pageable pageable);

    /**
     * Находит всех неактивных (уволенных) сотрудников.
     *
     * @return список неактивных сотрудников
     */
    List<T> findByTerminationDateIsNotNull();

    /**
     * Находит всех неактивных (уволенных) сотрудников с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница с неактивными сотрудниками
     */
    Page<T> findByTerminationDateIsNotNull(Pageable pageable);

    /**
     * Находит всех сотрудников с указанным типом занятости.
     *
     * @param employmentType тип занятости
     * @return список сотрудников с указанным типом занятости
     */
    List<T> findByEmploymentType(EmploymentType employmentType);

    /**
     * Находит всех сотрудников с указанным типом занятости с пагинацией.
     *
     * @param employmentType тип занятости
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанным типом занятости
     */
    Page<T> findByEmploymentType(EmploymentType employmentType, Pageable pageable);

    /**
     * Находит всех сотрудников с указанным графиком работы.
     *
     * @param workSchedule график работы
     * @return список сотрудников с указанным графиком работы
     */
    List<T> findByWorkSchedule(WorkSchedule workSchedule);

    /**
     * Находит всех сотрудников с указанным графиком работы с пагинацией.
     *
     * @param workSchedule график работы
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанным графиком работы
     */
    Page<T> findByWorkSchedule(WorkSchedule workSchedule, Pageable pageable);

    /**
     * Находит всех сотрудников с указанной заработной платой.
     *
     * @param salary заработная плата
     * @return список сотрудников с указанной заработной платой
     */
    List<T> findBySalary(Double salary);

    /**
     * Находит всех сотрудников с заработной платой в указанном диапазоне.
     *
     * @param minSalary минимальная заработная плата
     * @param maxSalary максимальная заработная плата
     * @return список сотрудников с заработной платой в указанном диапазоне
     */
    List<T> findBySalaryBetween(Double minSalary, Double maxSalary);

    /**
     * Находит всех сотрудников с заработной платой в указанном диапазоне с пагинацией.
     *
     * @param minSalary минимальная заработная плата
     * @param maxSalary максимальная заработная плата
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с заработной платой в указанном диапазоне
     */
    Page<T> findBySalaryBetween(Double minSalary, Double maxSalary, Pageable pageable);

    /**
     * Находит всех сотрудников с указанной валютой заработной платы.
     *
     * @param currency валюта заработная плата
     * @return список сотрудников с указанной валютой заработной платы
     */
    List<T> findByCurrency(String currency);

    /**
     * Находит всех сотрудников с указанной валютой заработной платы с пагинацией.
     *
     * @param currency валюта заработная плата
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанной валютой заработной платы
     */
    Page<T> findByCurrency(String currency, Pageable pageable);

    /**
     * Находит всех сотрудников с указанным руководителем.
     *
     * @param supervisorId идентификатор руководителя
     * @return список сотрудников с указанным руководителем
     */
    List<T> findBySupervisorId(Long supervisorId);

    /**
     * Находит всех сотрудников с указанным руководителем с пагинацией.
     *
     * @param supervisorId идентификатор руководителя
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с указанным руководителем
     */
    Page<T> findBySupervisorId(Long supervisorId, Pageable pageable);

    /**
     * Находит всех сотрудников по рабочему email.
     *
     * @param workEmail рабочий email
     * @return список сотрудников с указанным рабочим email
     */
    List<T> findByWorkEmail(String workEmail);

    /**
     * Находит всех сотрудников по рабочему телефону.
     *
     * @param workPhone рабочий телефон
     * @return список сотрудников с указанным рабочим телефоном
     */
    List<T> findByWorkPhone(String workPhone);

    /**
     * Находит всех сотрудников по местоположению офиса.
     *
     * @param officeLocation местоположение офиса
     * @return список сотрудников по указанному местоположению офиса
     */
    List<T> findByOfficeLocation(String officeLocation);

    /**
     * Находит всех сотрудников по местоположению офиса с пагинацией.
     *
     * @param officeLocation местоположение офиса
     * @param pageable параметры пагинации
     * @return страница с сотрудниками по указанному местоположению офиса
     */
    Page<T> findByOfficeLocation(String officeLocation, Pageable pageable);

    /**
     * Находит всех сотрудников по части табельного номера.
     *
     * @param employeeId часть табельного номера
     * @return список сотрудников, чей табельный номер содержит указанную строку
     */
    List<T> findByEmployeeIdContaining(String employeeId);

    /**
     * Находит всех сотрудников по части табельного номера с пагинацией.
     *
     * @param employeeId часть табельного номера
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, чей табельный номер содержит указанную строку
     */
    Page<T> findByEmployeeIdContaining(String employeeId, Pageable pageable);

    /**
     * Находит всех сотрудников по части должности.
     *
     * @param position часть должности
     * @return список сотрудников, чья должность содержит указанную строку
     */
    List<T> findByPositionContaining(String position);

    /**
     * Находит всех сотрудников по части должности с пагинацией.
     *
     * @param position часть должности
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, чья должность содержит указанную строку
     */
    Page<T> findByPositionContaining(String position, Pageable pageable);

    /**
     * Находит всех сотрудников по части названия отдела.
     *
     * @param department часть названия отдела
     * @return список сотрудников, чей отдел содержит указанную строку
     */
    List<T> findByDepartmentContaining(String department);

    /**
     * Находит всех сотрудников по части названия отдела с пагинацией.
     *
     * @param department часть названия отдела
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, чей отдел содержит указанную строку
     */
    Page<T> findByDepartmentContaining(String department, Pageable pageable);

    /**
     * Находит всех сотрудников, у которых день рождения в указанный месяц.
     *
     * @param month номер месяца (1-12)
     * @return список сотрудников, у которых день рождения в указанный месяц
     */
    @Query("SELECT e FROM #{#entityName} e WHERE FUNCTION('MONTH', e.birthDate) = :month AND e.deletedAt IS NULL")
    List<T> findByBirthDateMonth(@Param("month") int month);

    /**
     * Находит всех сотрудников, у которых день рождения в указанный месяц с пагинацией.
     *
     * @param month номер месяца (1-12)
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, у которых день рождения в указанный месяц
     */
    @Query("SELECT e FROM #{#entityName} e WHERE FUNCTION('MONTH', e.birthDate) = :month AND e.deletedAt IS NULL")
    Page<T> findByBirthDateMonth(@Param("month") int month, Pageable pageable);

    /**
     * Проверяет существование сотрудника с указанным табельным номером.
     *
     * @param employeeId табельный номер для проверки
     * @return true если сотрудник с таким табельным номером существует, false в противном случае
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * Проверяет существование сотрудника с указанным табельным номером (включая удаленных).
     *
     * @param employeeId табельный номер для проверки
     * @return true если сотрудник с таким табельным номером существует, false в противном случае
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.employeeId = :employeeId")
    boolean existsByEmployeeIdWithDeleted(@Param("employeeId") String employeeId);

    /**
     * Проверяет существование сотрудника с указанным рабочим email.
     *
     * @param workEmail рабочий email для проверки
     * @return true если сотрудник с таким рабочим email существует, false в противном случае
     */
    boolean existsByWorkEmail(String workEmail);

    /**
     * Проверяет существование сотрудника с указанным рабочим email (включая удаленных).
     *
     * @param workEmail рабочий email для проверки
     * @return true если сотрудник с таким рабочим email существует, false в противном случае
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.workEmail = :workEmail")
    boolean existsByWorkEmailWithDeleted(@Param("workEmail") String workEmail);

    /**
     * Находит количество активных сотрудников.
     *
     * @return количество активных сотрудников
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL")
    long countByTerminationDateIsNull();

    /**
     * Находит количество сотрудников по отделам.
     *
     * @return список пар (название отдела, количество сотрудников)
     */
    @Query("SELECT e.department, COUNT(e) FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL GROUP BY e.department")
    List<Object[]> countEmployeesByDepartment();

    /**
     * Находит количество сотрудников по типам занятости.
     *
     * @return список пар (тип занятости, количество сотрудников)
     */
    @Query("SELECT e.employmentType, COUNT(e) FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL GROUP BY e.employmentType")
    List<Object[]> countEmployeesByEmploymentType();

    /**
     * Находит среднюю заработную плату сотрудников.
     *
     * @return средняя заработная плата
     */
    @Query("SELECT AVG(e.salary) FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL")
    Double getAverageSalary();

    /**
     * Находит среднюю заработную плату сотрудников по отделам.
     *
     * @return список пар (название отдела, средняя заработная плата)
     */
    @Query("SELECT e.department, AVG(e.salary) FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL GROUP BY e.department")
    List<Object[]> getAverageSalaryByDepartment();

    /**
     * Находит сотрудников с наибольшей заработной платой.
     *
     * @param limit максимальное количество сотрудников
     * @return список сотрудников с наибольшей заработной платой
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL ORDER BY e.salary DESC")
    List<T> findTopPaidEmployees(@Param("limit") int limit);

    /**
     * Находит сотрудников с наибольшей заработной платой с пагинацией.
     *
     * @param limit максимальное количество сотрудников
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с наибольшей заработной платой
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL ORDER BY e.salary DESC")
    Page<T> findTopPaidEmployees(@Param("limit") int limit, Pageable pageable);

    /**
     * Находит сотрудников с наименьшей заработной платой.
     *
     * @param limit максимальное количество сотрудников
     * @return список сотрудников с наименьшей заработной платой
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL ORDER BY e.salary ASC")
    List<T> findLowestPaidEmployees(@Param("limit") int limit);

    /**
     * Находит сотрудников с наименьшей заработной платой с пагинацией.
     *
     * @param limit максимальное количество сотрудников
     * @param pageable параметры пагинации
     * @return страница с сотрудниками с наименьшей заработной платой
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL ORDER BY e.salary ASC")
    Page<T> findLowestPaidEmployees(@Param("limit") int limit, Pageable pageable);

    /**
     * Находит количество подчиненных у указанного руководителя.
     *
     * @param supervisorId идентификатор руководителя
     * @return количество подчиненных
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.supervisorId = :supervisorId AND e.terminationDate IS NULL AND e.deletedAt IS NULL")
    long countBySupervisorId(@Param("supervisorId") Long supervisorId);

    /**
     * Находит сотрудников, у которых срок работы превышает указанное количество лет.
     *
     * @param minDate минимальная дата приема на работу
     * @return список сотрудников со стажем более указанного количества лет
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "e.hireDate <= :minDate AND e.terminationDate IS NULL AND e.deletedAt IS NULL")
    List<T> findEmployeesWithExperienceMoreThanYears(@Param("minDate") LocalDate minDate);

    /**
     * Находит сотрудников, у которых срок работы превышает указанное количество лет с пагинацией.
     *
     * @param minDate минимальная дата приема на работу
     * @param pageable параметры пагинации
     * @return страница с сотрудниками со стажем более указанного количества лет
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "e.hireDate <= :minDate AND e.terminationDate IS NULL AND e.deletedAt IS NULL")
    Page<T> findEmployeesWithExperienceMoreThanYears(@Param("minDate") LocalDate minDate, Pageable pageable);

    /**
     * Находит сотрудников по нескольким критериям одновременно.
     *
     * @param department отдел
     * @param position должность
     * @param employmentType тип занятости
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, соответствующими критериям
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "(:department IS NULL OR e.department = :department) AND " +
            "(:position IS NULL OR e.position = :position) AND " +
            "(:employmentType IS NULL OR e.employmentType = :employmentType) AND " +
            "e.terminationDate IS NULL AND e.deletedAt IS NULL")
    Page<T> findByMultipleCriteria(@Param("department") String department,
                                   @Param("position") String position,
                                   @Param("employmentType") EmploymentType employmentType,
                                   Pageable pageable);

    /**
     * Находит сотрудников с высокой зарплатой (выше средней на определенный процент).
     *
     * @param percentage процент превышения над средней зарплатой
     * @param pageable параметры пагинации
     * @return страница с высокооплачиваемыми сотрудниками
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "e.salary > (SELECT AVG(e2.salary) * (1 + :percentage / 100.0) FROM #{#entityName} e2 WHERE e2.salary IS NOT NULL) AND " +
            "e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL")
    Page<T> findHighEarners(@Param("percentage") double percentage, Pageable pageable);

    /**
     * Находит сотрудников с низкой зарплатой (ниже средней на определенный процент).
     *
     * @param percentage процент отклонения от средней зарплаты
     * @param pageable параметры пагинации
     * @return страница с низкооплачиваемыми сотрудниками
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "e.salary < (SELECT AVG(e2.salary) * (1 - :percentage / 100.0) FROM #{#entityName} e2 WHERE e2.salary IS NOT NULL) AND " +
            "e.terminationDate IS NULL AND e.deletedAt IS NULL AND e.salary IS NOT NULL")
    Page<T> findLowEarners(@Param("percentage") double percentage, Pageable pageable);

    /**
     * Находит сотрудников с длинным стажем работы.
     *
     * @param pageable параметры пагинации
     * @return страница с опытными сотрудниками
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "e.hireDate <= :yearsAgo AND e.terminationDate IS NULL AND e.deletedAt IS NULL")
    Page<T> findExperiencedEmployees(@Param("yearsAgo") LocalDate yearsAgo, Pageable pageable);

    /**
     * Находит сотрудников, у которых скоро день рождения.
     *
     * @param days количество дней вперед для поиска
     * @param pageable параметры пагинации
     * @return страница с сотрудниками, у которых скоро день рождения
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "FUNCTION('DAYOFYEAR', e.birthDate) BETWEEN FUNCTION('DAYOFYEAR', CURRENT_DATE) AND FUNCTION('DAYOFYEAR', CURRENT_DATE) + :days AND " +
            "e.terminationDate IS NULL AND e.deletedAt IS NULL")
    Page<T> findUpcomingBirthdays(@Param("days") int days, Pageable pageable);
}