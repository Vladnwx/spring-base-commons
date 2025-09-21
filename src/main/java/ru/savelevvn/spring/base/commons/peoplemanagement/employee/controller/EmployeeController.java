package ru.savelevvn.spring.base.commons.peoplemanagement.employee.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.Employee;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.EmploymentType;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.WorkSchedule;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.service.EmployeeService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Базовый REST контроллер для работы с сотрудниками.
 * Расширяет PersonController, добавляя методы, специфичные для сотрудников.
 *
 * @param <T> тип сущности, расширяющей Employee
 * @param <S> тип сервиса, расширяющего EmployeeService
 * @version 1.0
 */
@Slf4j
public abstract class EmployeeController<T extends Employee, S extends EmployeeService<T, ?>>
        extends ru.savelevvn.spring.base.commons.peoplemanagement.controller.PersonController<T, S> {

    /**
     * Конструктор контроллера.
     *
     * @param service сервис для работы с сотрудниками
     */
    protected EmployeeController(S service) {
        super(service);
    }

    // Приведение типа для доступа к методам EmployeeService
    @SuppressWarnings("unchecked")
    public S getService() {
        return (S) super.getService();
    }

    /**
     * Поиск сотрудника по табельному номеру.
     *
     * @param employeeId табельный номер сотрудника
     * @return найденный сотрудник или 404 если не найден
     */
    @GetMapping("/employeeId/{employeeId}")
    public ResponseEntity<T> getByEmployeeId(@PathVariable String employeeId) {
        log.debug("Поиск сотрудника по табельному номеру: {}", employeeId);

        try {
            Optional<T> employee = getService().findByEmployeeId(employeeId);
            if (employee.isPresent()) {
                log.info("Сотрудник найден по табельному номеру: {}", employeeId);
                T processedEmployee = getPostProcess().apply(employee.get());
                return ResponseEntity.ok(processedEmployee);
            } else {
                log.info("Сотрудник не найден по табельному номеру: {}", employeeId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудника по табельному номеру {}: ", employeeId, e);
            return (ResponseEntity<T>) handleException(e, "поиск сотрудника по табельному номеру " + employeeId);
        }
    }

    /**
     * Поиск сотрудников по должности с пагинацией.
     *
     * @param position должность для поиска
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с сотрудниками
     */
    @GetMapping("/search/position/{position}")
    public ResponseEntity<Page<T>> searchByPosition(
            @PathVariable String position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск сотрудников по должности: {}, страница={}, размер={}", position, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            Page<T> employees = getService().findByPosition(position, pageable);
            Page<T> processedEmployees = getPostProcessPage().apply(employees);

            log.info("Найдено {} сотрудников по должности: {}", employees.getNumberOfElements(), position);
            return ResponseEntity.ok(processedEmployees);
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по должности {}: ", position, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск сотрудников по должности " + position);
        }
    }

    /**
     * Поиск сотрудников по отделу с пагинацией.
     *
     * @param department отдел для поиска
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с сотрудниками
     */
    @GetMapping("/search/department/{department}")
    public ResponseEntity<Page<T>> searchByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск сотрудников по отделу: {}, страница={}, размер={}", department, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            Page<T> employees = getService().findByDepartment(department, pageable);
            Page<T> processedEmployees = getPostProcessPage().apply(employees);

            log.info("Найдено {} сотрудников по отделу: {}", employees.getNumberOfElements(), department);
            return ResponseEntity.ok(processedEmployees);
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по отделу {}: ", department, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск сотрудников по отделу " + department);
        }
    }

    /**
     * Поиск сотрудников по типу занятости.
     *
     * @param employmentType тип занятости
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с сотрудниками
     */
    @GetMapping("/search/employmentType/{employmentType}")
    public ResponseEntity<Page<T>> searchByEmploymentType(
            @PathVariable EmploymentType employmentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск сотрудников по типу занятости: {}, страница={}, размер={}", employmentType, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            Page<T> employees = getService().findByEmploymentType(employmentType, pageable);
            Page<T> processedEmployees = getPostProcessPage().apply(employees);

            log.info("Найдено {} сотрудников по типу занятости: {}", employees.getNumberOfElements(), employmentType);
            return ResponseEntity.ok(processedEmployees);
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по типу занятости {}: ", employmentType, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск сотрудников по типу занятости " + employmentType);
        }
    }

    /**
     * Поиск сотрудников по графику работы.
     *
     * @param workSchedule график работы
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с сотрудниками
     */
    @GetMapping("/search/workSchedule/{workSchedule}")
    public ResponseEntity<Page<T>> searchByWorkSchedule(
            @PathVariable WorkSchedule workSchedule,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск сотрудников по графику работы: {}, страница={}, размер={}", workSchedule, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            Page<T> employees = getService().findByWorkSchedule(workSchedule, pageable);
            Page<T> processedEmployees = getPostProcessPage().apply(employees);

            log.info("Найдено {} сотрудников по графику работы: {}", employees.getNumberOfElements(), workSchedule);
            return ResponseEntity.ok(processedEmployees);
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по графику работы {}: ", workSchedule, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск сотрудников по графику работы " + workSchedule);
        }
    }

    /**
     * Поиск сотрудников по диапазону зарплат.
     *
     * @param minSalary минимальная зарплата
     * @param maxSalary максимальная зарплата
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с сотрудниками
     */
    @GetMapping("/search/salary")
    public ResponseEntity<Page<T>> searchBySalaryRange(
            @RequestParam Double minSalary,
            @RequestParam Double maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск сотрудников по диапазону зарплат: {} - {}, страница={}, размер={}",
                minSalary, maxSalary, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            Page<T> employees = getService().findBySalaryBetween(minSalary, maxSalary, pageable);
            Page<T> processedEmployees = getPostProcessPage().apply(employees);

            log.info("Найдено {} сотрудников по диапазону зарплат: {} - {}",
                    employees.getNumberOfElements(), minSalary, maxSalary);
            return ResponseEntity.ok(processedEmployees);
        } catch (Exception e) {
            log.error("Ошибка при поиске сотрудников по диапазону зарплат {} - {}: ", minSalary, maxSalary, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск сотрудников по диапазону зарплат");
        }
    }

    /**
     * Поиск активных сотрудников.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с активными сотрудниками
     */
    @GetMapping("/search/active")
    public ResponseEntity<Page<T>> searchActiveEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск активных сотрудников, страница={}, размер={}", page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            Page<T> employees = getService().findActiveEmployees(pageable);
            Page<T> processedEmployees = getPostProcessPage().apply(employees);

            log.info("Найдено {} активных сотрудников", employees.getNumberOfElements());
            return ResponseEntity.ok(processedEmployees);
        } catch (Exception e) {
            log.error("Ошибка при поиске активных сотрудников: ", e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск активных сотрудников");
        }
    }

    /**
     * Поиск неактивных (уволенных) сотрудников.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с неактивными сотрудниками
     */
    @GetMapping("/search/inactive")
    public ResponseEntity<Page<T>> searchInactiveEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск неактивных сотрудников, страница={}, размер={}", page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            Page<T> employees = getService().findInactiveEmployees(pageable);
            Page<T> processedEmployees = getPostProcessPage().apply(employees);

            log.info("Найдено {} неактивных сотрудников", employees.getNumberOfElements());
            return ResponseEntity.ok(processedEmployees);
        } catch (Exception e) {
            log.error("Ошибка при поиске неактивных сотрудников: ", e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск неактивных сотрудников");
        }
    }

    /**
     * Создание нового сотрудника.
     *
     * @param employee данные нового сотрудника
     * @return созданный сотрудник
     */
    @PostMapping("/create")
    public ResponseEntity<T> createEmployee(@Valid @RequestBody T employee) {
        log.debug("Создание нового сотрудника: {}", employee);

        try {
            T createdEmployee = getService().createEmployee(employee);
            T processedEmployee = getPostProcess().apply(createdEmployee);

            log.info("Сотрудник успешно создан с ID: {}", createdEmployee.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(processedEmployee);
        } catch (Exception e) {
            log.error("Ошибка при создании сотрудника: ", e);
            return (ResponseEntity<T>) handleException(e, "создание сотрудника");
        }
    }

    /**
     * Увольнение сотрудника.
     *
     * @param id идентификатор сотрудника
     * @param terminationDate дата увольнения
     * @param reason причина увольнения
     * @return уволенный сотрудник
     */
    @PostMapping("/{id}/terminate")
    public ResponseEntity<T> terminateEmployee(
            @PathVariable Long id,
            @RequestParam LocalDate terminationDate,
            @RequestParam String reason) {

        log.debug("Увольнение сотрудника с ID {}: дата={}, причина={}", id, terminationDate, reason);

        try {
            T terminatedEmployee = getService().terminateEmployee(id, terminationDate, reason);
            T processedEmployee = getPostProcess().apply(terminatedEmployee);

            log.info("Сотрудник успешно уволен с ID: {}", id);
            return ResponseEntity.ok(processedEmployee);
        } catch (Exception e) {
            log.error("Ошибка при увольнении сотрудника с ID {}: ", id, e);
            return (ResponseEntity<T>) handleException(e, "увольнение сотрудника с ID " + id);
        }
    }

    /**
     * Активация (восстановление) сотрудника.
     *
     * @param id идентификатор сотрудника
     * @return активированный сотрудник
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<T> activateEmployee(@PathVariable Long id) {
        log.debug("Активация сотрудника с ID: {}", id);

        try {
            T activatedEmployee = getService().activateEmployee(id);
            T processedEmployee = getPostProcess().apply(activatedEmployee);

            log.info("Сотрудник успешно активирован с ID: {}", id);
            return ResponseEntity.ok(processedEmployee);
        } catch (Exception e) {
            log.error("Ошибка при активации сотрудника с ID {}: ", id, e);
            return (ResponseEntity<T>) handleException(e, "активация сотрудника с ID " + id);
        }
    }

    /**
     * Назначение руководителя сотруднику.
     *
     * @param employeeId идентификатор сотрудника
     * @param supervisorId идентификатор руководителя
     * @return обновленный сотрудник
     */
    @PostMapping("/{employeeId}/assign-supervisor/{supervisorId}")
    public ResponseEntity<T> assignSupervisor(
            @PathVariable Long employeeId,
            @PathVariable Long supervisorId) {

        log.debug("Назначение руководителя {} сотруднику {}", supervisorId, employeeId);

        try {
            T updatedEmployee = getService().assignSupervisor(employeeId, supervisorId);
            T processedEmployee = getPostProcess().apply(updatedEmployee);

            log.info("Руководитель {} успешно назначен сотруднику {}", supervisorId, employeeId);
            return ResponseEntity.ok(processedEmployee);
        } catch (Exception e) {
            log.error("Ошибка при назначении руководителя {} сотруднику {}: ", supervisorId, employeeId, e);
            return (ResponseEntity<T>) handleException(e, "назначение руководителя сотруднику");
        }
    }

    /**
     * Обновление заработной платы сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @param salary новая заработная плата
     * @param currency валюта
     * @return обновленный сотрудник
     */
    @PostMapping("/{employeeId}/update-salary")
    public ResponseEntity<T> updateSalary(
            @PathVariable Long employeeId,
            @RequestParam Double salary,
            @RequestParam String currency) {

        log.debug("Обновление зарплаты сотрудника с ID {}: {} {}", employeeId, salary, currency);

        try {
            T updatedEmployee = getService().updateSalary(employeeId, salary, currency);
            T processedEmployee = getPostProcess().apply(updatedEmployee);

            log.info("Зарплата успешно обновлена для сотрудника с ID: {}", employeeId);
            return ResponseEntity.ok(processedEmployee);
        } catch (Exception e) {
            log.error("Ошибка при обновлении зарплаты сотрудника с ID {}: ", employeeId, e);
            return (ResponseEntity<T>) handleException(e, "обновление зарплаты сотрудника с ID " + employeeId);
        }
    }

    /**
     * Получение стажа работы сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @return стаж работы в годах
     */
    @GetMapping("/{employeeId}/years-of-service")
    public ResponseEntity<Integer> getYearsOfService(@PathVariable Long employeeId) {
        log.debug("Получение стажа работы сотрудника с ID: {}", employeeId);

        try {
            int years = getService().getYearsOfService(employeeId);
            log.info("Стаж работы сотрудника с ID {} составляет {} лет", employeeId, years);
            return ResponseEntity.ok(years);
        } catch (Exception e) {
            log.error("Ошибка при получении стажа работы сотрудника с ID {}: ", employeeId, e);
            return (ResponseEntity<Integer>) handleException(e, "получение стажа работы сотрудника с ID " + employeeId);
        }
    }

    /**
     * Проверка, является ли сотрудник руководителем.
     *
     * @param employeeId идентификатор сотрудника
     * @return true если сотрудник является руководителем, false в противном случае
     */
    @GetMapping("/{employeeId}/is-supervisor")
    public ResponseEntity<Boolean> isSupervisor(@PathVariable Long employeeId) {
        log.debug("Проверка, является ли сотрудник с ID {} руководителем", employeeId);

        try {
            boolean result = getService().isSupervisor(employeeId);
            log.info("Сотрудник с ID {} {} является руководителем", employeeId, result ? "" : "не");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при проверке, является ли сотрудник с ID {} руководителем: ", employeeId, e);
            return (ResponseEntity<Boolean>) handleException(e, "проверка руководителя с ID " + employeeId);
        }
    }

    /**
     * Получение количества активных сотрудников.
     *
     * @return количество активных сотрудников
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveEmployees() {
        log.debug("Получение количества активных сотрудников");

        try {
            long count = getService().countActiveEmployees();
            log.info("Количество активных сотрудников: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Ошибка при получении количества активных сотрудников: ", e);
            return (ResponseEntity<Long>) handleException(e, "получение количества активных сотрудников");
        }
    }

    /**
     * Обновить сотрудника.
     *
     * @param id идентификатор сотрудника
     * @param employee обновленные данные сотрудника
     * @return обновленный сотрудник
     */
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<T> update(@PathVariable Long id, @Valid @RequestBody T employee) {
        log.debug("Обновление сотрудника с ID {}: {}", id, employee);

        try {
            validateId(id);

            if (!getService().existsById(id)) {
                log.info("Попытка обновления несуществующего сотрудника с ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Валидация
            getService().validateEmployee(employee);

            // Обновление
            T updatedEntity = getService().updateEmployee(id, employee);

            // Постобработка
            T resultEntity = getPostProcess().apply(updatedEntity);

            log.info("Сотрудник успешно обновлен с ID: {}", id);
            return ResponseEntity.ok(resultEntity);
        } catch (Exception e) {
            log.error("Ошибка при обновлении сотрудника с ID {}: ", id, e);
            return (ResponseEntity<T>) handleException(e, "обновление сотрудника с ID " + id);
        }
    }
}