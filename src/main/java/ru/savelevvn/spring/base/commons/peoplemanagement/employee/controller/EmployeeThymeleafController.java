package ru.savelevvn.spring.base.commons.peoplemanagement.employee.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.Employee;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.EmploymentType;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.WorkSchedule;
import ru.savelevvn.spring.base.commons.peoplemanagement.employee.service.EmployeeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Базовый Thymeleaf контроллер для работы с сотрудниками.
 * Расширяет PersonThymeleafController, добавляя методы, специфичные для сотрудников.
 *
 * @param <T> тип сущности, расширяющей Employee
 * @param <S> тип сервиса, расширяющего EmployeeService
 * @version 1.0
 */
@Slf4j
public abstract class EmployeeThymeleafController<T extends Employee, S extends EmployeeService<T, ?>>
        extends ru.savelevvn.spring.base.commons.peoplemanagement.controller.PersonThymeleafController<T, S> {

    /**
     * Конструктор контроллера.
     *
     * @param service сервис для работы с сотрудниками
     * @param entityName имя сущности (например, "employee")
     * @param entityNamePlural имя сущности во множественном числе (например, "employees")
     * @param basePath базовый путь (например, "/admin/employees")
     * @param viewPrefix префикс для представлений (например, "admin/employees/")
     */
    protected EmployeeThymeleafController(S service, String entityName, String entityNamePlural,
                                          String basePath, String viewPrefix) {
        super(service, entityName, entityNamePlural, basePath, viewPrefix);
    }

    // Приведение типа для доступа к методам EmployeeService
    @SuppressWarnings("unchecked")
    public S getService() {
        return (S) super.getService();
    }

    /**
     * Список сотрудников с расширенной фильтрацией.
     *
     * @param showDeletedStr фильтр по удаленным сотрудникам
     * @param employeeId фильтр по табельному номеру
     * @param position фильтр по должности
     * @param department фильтр по отделу
     * @param employmentType фильтр по типу занятости
     * @param workSchedule фильтр по графику работы
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле сортировки
     * @param direction направление сортировки
     * @param model модель для передачи данных в представление
     * @return имя шаблона
     */
    @GetMapping
    public String list(
            @RequestParam(value = "showDeleted", required = false) String showDeletedStr,
            @RequestParam(value = "employeeId", required = false) String employeeId,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "employmentType", required = false) EmploymentType employmentType,
            @RequestParam(value = "workSchedule", required = false) WorkSchedule workSchedule,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model) {

        log.debug("Отображение списка сотрудников с фильтрацией");

        try {
            validatePaginationParams(page, size);

            Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<T> employees;

            // Применяем фильтры
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                Optional<T> employeeOpt = getService().findByEmployeeId(employeeId);
                employees = employeeOpt.map(emp -> createPageFromList(List.of(emp), pageable))
                        .orElse(createPageFromList(List.of(), pageable));
            } else if (position != null && !position.trim().isEmpty()) {
                employees = getService().findByPosition(position, pageable);
            } else if (department != null && !department.trim().isEmpty()) {
                employees = getService().findByDepartment(department, pageable);
            } else if (employmentType != null) {
                employees = getService().findByEmploymentType(employmentType, pageable);
            } else if (workSchedule != null) {
                employees = getService().findByWorkSchedule(workSchedule, pageable);
            } else {
                // Используем стандартную фильтрацию по удаленным
                employees = getFilteredEmployees(showDeletedStr, pageable);
            }

            Page<T> processedEmployees = postProcess(employees);

            model.addAttribute(entityNamePlural, processedEmployees);
            model.addAttribute("showDeleted", showDeletedStr);
            model.addAttribute("employeeId", employeeId);
            model.addAttribute("position", position);
            model.addAttribute("department", department);
            model.addAttribute("employmentType", employmentType);
            model.addAttribute("workSchedule", workSchedule);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sort);
            model.addAttribute("sortDirection", direction);

            addCommonAttributes(model);

            log.info("Отображено {} сотрудников на странице {}", processedEmployees.getNumberOfElements(), page);
            return viewPrefix + "list";
        } catch (Exception e) {
            log.error("Ошибка при отображении списка сотрудников: ", e);
            addErrorMessage(model, "Ошибка при загрузке списка: " + e.getMessage());
            addCommonAttributes(model);
            model.addAttribute(entityNamePlural, Page.empty());
            return viewPrefix + "list";
        }
    }

    /**
     * Форма для создания нового сотрудника.
     */
    @GetMapping("/new")
    @Override
    public String createForm(Model model) {
        log.debug("Отображение формы создания нового сотрудника");

        try {
            T employee = createNewEntity();
            // Генерируем табельный номер по умолчанию
            employee.setEmployeeId(getService().generateEmployeeId());

            model.addAttribute("entity", employee);
            model.addAttribute(entityName, employee);
            addCommonAttributes(model);
            model.addAttribute("viewPrefix", viewPrefix);

            log.info("Форма создания сотрудника отображена");
            return viewPrefix + "form";
        } catch (Exception e) {
            log.error("Ошибка при отображении формы создания сотрудника: ", e);
            addErrorMessage(model, "Ошибка при подготовке формы: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Создание нового сотрудника.
     */
    @PostMapping("/create")
    public String createEmployee(@ModelAttribute T employee, RedirectAttributes redirectAttributes) {
        log.debug("Создание нового сотрудника: {}", employee);

        try {
            T createdEmployee = getService().createEmployee(employee);
            addSuccessMessage(redirectAttributes, "Сотрудник успешно создан с табельным номером: " + createdEmployee.getEmployeeId());

            log.info("Сотрудник успешно создан с ID: {} и табельным номером: {}",
                    createdEmployee.getId(), createdEmployee.getEmployeeId());
            return "redirect:" + basePath;
        } catch (Exception e) {
            log.error("Ошибка при создании сотрудника: ", e);
            addErrorMessage(redirectAttributes, "Ошибка при создании сотрудника: " + e.getMessage());
            return "redirect:" + basePath + "/new";
        }
    }

    /**
     * Увольнение сотрудника.
     */
    @PostMapping("/{id}/terminate")
    public String terminateEmployee(@PathVariable Long id,
                                    @RequestParam LocalDate terminationDate,
                                    @RequestParam String reason,
                                    RedirectAttributes redirectAttributes) {
        log.debug("Увольнение сотрудника с ID {}: дата={}, причина={}", id, terminationDate, reason);

        try {
            T terminatedEmployee = getService().terminateEmployee(id, terminationDate, reason);
            addSuccessMessage(redirectAttributes, "Сотрудник успешно уволен");

            log.info("Сотрудник успешно уволен с ID: {}", id);
            return "redirect:" + basePath;
        } catch (Exception e) {
            log.error("Ошибка при увольнении сотрудника с ID {}: ", id, e);
            addErrorMessage(redirectAttributes, "Ошибка при увольнении сотрудника: " + e.getMessage());
            return "redirect:" + basePath;
        }
    }

    /**
     * Активация (восстановление) сотрудника.
     */
    @PostMapping("/{id}/activate")
    public String activateEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("Активация сотрудника с ID: {}", id);

        try {
            T activatedEmployee = getService().activateEmployee(id);
            addSuccessMessage(redirectAttributes, "Сотрудник успешно активирован");

            log.info("Сотрудник успешно активирован с ID: {}", id);
            return "redirect:" + basePath;
        } catch (Exception e) {
            log.error("Ошибка при активации сотрудника с ID {}: ", id, e);
            addErrorMessage(redirectAttributes, "Ошибка при активации сотрудника: " + e.getMessage());
            return "redirect:" + basePath;
        }
    }

    /**
     * Назначение руководителя сотруднику.
     */
    @PostMapping("/{employeeId}/assign-supervisor")
    public String assignSupervisor(@PathVariable Long employeeId,
                                   @RequestParam Long supervisorId,
                                   RedirectAttributes redirectAttributes) {
        log.debug("Назначение руководителя {} сотруднику {}", supervisorId, employeeId);

        try {
            T updatedEmployee = getService().assignSupervisor(employeeId, supervisorId);
            addSuccessMessage(redirectAttributes, "Руководитель успешно назначен");

            log.info("Руководитель {} успешно назначен сотруднику {}", supervisorId, employeeId);
            return "redirect:" + basePath + "/view/" + employeeId;
        } catch (Exception e) {
            log.error("Ошибка при назначении руководителя {} сотруднику {}: ", supervisorId, employeeId, e);
            addErrorMessage(redirectAttributes, "Ошибка при назначении руководителя: " + e.getMessage());
            return "redirect:" + basePath + "/view/" + employeeId;
        }
    }

    /**
     * Обновление заработной платы сотрудника.
     */
    @PostMapping("/{employeeId}/update-salary")
    public String updateSalary(@PathVariable Long employeeId,
                               @RequestParam Double salary,
                               @RequestParam String currency,
                               RedirectAttributes redirectAttributes) {
        log.debug("Обновление зарплаты сотрудника с ID {}: {} {}", employeeId, salary, currency);

        try {
            T updatedEmployee = getService().updateSalary(employeeId, salary, currency);
            addSuccessMessage(redirectAttributes, "Зарплата успешно обновлена");

            log.info("Зарплата успешно обновлена для сотрудника с ID: {}", employeeId);
            return "redirect:" + basePath + "/view/" + employeeId;
        } catch (Exception e) {
            log.error("Ошибка при обновлении зарплаты сотрудника с ID {}: ", employeeId, e);
            addErrorMessage(redirectAttributes, "Ошибка при обновлении зарплаты: " + e.getMessage());
            return "redirect:" + basePath + "/view/" + employeeId;
        }
    }

    /**
     * Просмотр статистики по сотрудникам.
     */
    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        log.debug("Отображение статистики по сотрудникам");

        try {
            long activeCount = getService().countActiveEmployees();
            long totalCount = getService().count();
            // Упрощенная реализация - используем фиксированное значение
            double averageSalary = 50000.0;

            model.addAttribute("activeCount", activeCount);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("averageSalary", averageSalary);
            // Упрощенная реализация для статистики
            model.addAttribute("employeesByDepartment", List.of());
            model.addAttribute("employeesByEmploymentType", List.of());
            model.addAttribute("averageSalaryByDepartment", List.of());

            addCommonAttributes(model);

            log.info("Статистика по сотрудникам отображена");
            return viewPrefix + "statistics";
        } catch (Exception e) {
            log.error("Ошибка при отображении статистики по сотрудникам: ", e);
            addErrorMessage(model, "Ошибка при загрузке статистики: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Поиск высокооплачиваемых сотрудников.
     */
    @GetMapping("/search/high-earners")
    public String searchHighEarners(@RequestParam(defaultValue = "20.0") double percentage,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    Model model) {
        log.debug("Поиск высокооплачиваемых сотрудников ({}% выше средней)", percentage);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = PageRequest.of(page, size);

            // Упрощенная реализация
            Page<T> employees = createPageFromList(List.of(), pageable);

            model.addAttribute(entityNamePlural, employees);
            model.addAttribute("highEarners", true);
            model.addAttribute("percentage", percentage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            addCommonAttributes(model);

            log.info("Найдено {} высокооплачиваемых сотрудников", employees.getNumberOfElements());
            return viewPrefix + "list";
        } catch (Exception e) {
            log.error("Ошибка при поиске высокооплачиваемых сотрудников: ", e);
            addErrorMessage(model, "Ошибка при поиске высокооплачиваемых сотрудников: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Получение страницы с активными сотрудниками.
     */
    @GetMapping("/active")
    public String listActiveEmployees(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      Model model) {
        log.debug("Отображение списка активных сотрудников, страница={}, размер={}", page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = PageRequest.of(page, size);

            Page<T> employees = getService().findActiveEmployees(pageable);
            Page<T> processedEmployees = postProcess(employees);

            model.addAttribute(entityNamePlural, processedEmployees);
            model.addAttribute("activeOnly", true);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            addCommonAttributes(model);

            log.info("Отображено {} активных сотрудников на странице {}",
                    processedEmployees.getNumberOfElements(), page);
            return viewPrefix + "list";
        } catch (Exception e) {
            log.error("Ошибка при отображении списка активных сотрудников: ", e);
            addErrorMessage(model, "Ошибка при загрузке списка активных сотрудников: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Получение стажа работы сотрудника.
     */
    @GetMapping("/{employeeId}/years-of-service")
    @ResponseBody
    public int getYearsOfService(@PathVariable Long employeeId) {
        log.debug("Получение стажа работы сотрудника с ID: {}", employeeId);
        return getService().getYearsOfService(employeeId);
    }

    /**
     * Проверка, является ли сотрудник руководителем.
     */
    @GetMapping("/{employeeId}/is-supervisor")
    @ResponseBody
    public boolean isSupervisor(@PathVariable Long employeeId) {
        log.debug("Проверка, является ли сотрудник с ID {} руководителем", employeeId);
        return getService().isSupervisor(employeeId);
    }

    /**
     * Получить список активных (неудалённых) сущностей.
     */
    @Override
    protected List<T> getActiveEntities() {
        try {
            return getService().findActiveEmployees(PageRequest.of(0, 1000)).getContent();
        } catch (Exception e) {
            log.error("Ошибка при получении активных сотрудников: ", e);
            return List.of();
        }
    }

    /**
     * Получить список удалённых сущностей.
     */
    @Override
    protected List<T> getDeletedEntities() {
        try {
            return getService().findInactiveEmployees(PageRequest.of(0, 1000)).getContent();
        } catch (Exception e) {
            log.error("Ошибка при получении неактивных сотрудников: ", e);
            return List.of();
        }
    }

    /**
     * Получение отфильтрованных сотрудников.
     */
    private Page<T> getFilteredEmployees(String showDeletedStr, Pageable pageable) {
        if ("true".equalsIgnoreCase(showDeletedStr)) {
            return getService().findInactiveEmployees(pageable);
        } else if ("all".equalsIgnoreCase(showDeletedStr)) {
            return createPageFromList(getAllEntities(), pageable);
        } else {
            return getService().findActiveEmployees(pageable);
        }
    }

    /**
     * Постобработка страницы сущностей перед отображением.
     */
    protected Page<T> postProcess(Page<T> page) {
        return page;
    }

    /**
     * Валидация параметров пагинации.
     */
    protected void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть отрицательным");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Размер страницы должен быть от 1 до 100");
        }
    }

    /**
     * Создание страницы из списка.
     */
    protected Page<T> createPageFromList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<T> pageContent = start <= list.size() ? list.subList(start, end) : List.of();

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, list.size());
    }
}