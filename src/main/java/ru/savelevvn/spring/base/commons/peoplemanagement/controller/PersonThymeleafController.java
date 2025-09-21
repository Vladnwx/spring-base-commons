package ru.savelevvn.spring.base.commons.peoplemanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.savelevvn.spring.base.commons.BaseThymeleafController;
import ru.savelevvn.spring.base.commons.peoplemanagement.Gender;
import ru.savelevvn.spring.base.commons.peoplemanagement.MaritalStatus;
import ru.savelevvn.spring.base.commons.peoplemanagement.Person;
import ru.savelevvn.spring.base.commons.peoplemanagement.service.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Базовый Thymeleaf контроллер для работы с персонами.
 * Предоставляет веб-интерфейс для управления информацией о людях.
 *
 * @param <T> тип сущности, расширяющей Person
 * @param <S> тип сервиса, расширяющего PersonService
 * @version 1.0
 */
@Slf4j
public abstract class PersonThymeleafController<T extends Person, S extends PersonService<T, ?>>
        extends BaseThymeleafController<T, Long, S> {

    /**
     * Конструктор контроллера.
     *
     * @param service сервис для работы с персонами
     * @param entityName имя сущности (например, "person")
     * @param entityNamePlural имя сущности во множественном числе (например, "persons")
     * @param basePath базовый путь (например, "/admin/persons")
     * @param viewPrefix префикс для представлений (например, "admin/persons/")
     */
    protected PersonThymeleafController(S service, String entityName, String entityNamePlural,
                                        String basePath, String viewPrefix) {
        super(service, entityName, entityNamePlural, basePath, viewPrefix);
    }

    /**
     * Список персон с расширенной фильтрацией.
     *
     * @param showDeletedStr фильтр по удаленным персонам
     * @param firstName фильтр по имени
     * @param lastName фильтр по фамилии
     * @param email фильтр по email
     * @param gender фильтр по полу
     * @param maritalStatus фильтр по семейному положению
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
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "gender", required = false) Gender gender,
            @RequestParam(value = "maritalStatus", required = false) MaritalStatus maritalStatus,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model) {

        log.debug("Отображение списка персон с фильтрацией");

        try {
            validatePaginationParams(page, size);

            Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<T> persons;

            // Применяем фильтры
            if (firstName != null && !firstName.trim().isEmpty()) {
                List<T> allPersons = getService().findByFirstName(firstName);
                persons = createPageFromList(allPersons, pageable);
            } else if (lastName != null && !lastName.trim().isEmpty()) {
                List<T> allPersons = getService().findByLastName(lastName);
                persons = createPageFromList(allPersons, pageable);
            } else if (email != null && !email.trim().isEmpty()) {
                Optional<T> personOpt = getService().findByEmail(email);
                List<T> personsList = personOpt.map(List::of).orElse(List.of());
                persons = createPageFromList(personsList, pageable);
            } else if (gender != null) {
                List<T> allPersons = getService().findByGender(gender);
                persons = createPageFromList(allPersons, pageable);
            } else if (maritalStatus != null) {
                List<T> allPersons = getService().findByMaritalStatus(maritalStatus);
                persons = createPageFromList(allPersons, pageable);
            } else {
                // Используем стандартную фильтрацию по удаленным
                if ("true".equalsIgnoreCase(showDeletedStr)) {
                    List<T> allPersons = getDeletedEntities();
                    persons = createPageFromList(allPersons, pageable);
                } else if ("all".equalsIgnoreCase(showDeletedStr)) {
                    List<T> allPersons = getAllEntities();
                    persons = createPageFromList(allPersons, pageable);
                } else {
                    List<T> allPersons = getActiveEntities();
                    persons = createPageFromList(allPersons, pageable);
                }
            }

            Page<T> processedPersons = postProcess(persons);

            model.addAttribute(entityNamePlural, processedPersons);
            model.addAttribute("showDeleted", showDeletedStr);
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("email", email);
            model.addAttribute("gender", gender);
            model.addAttribute("maritalStatus", maritalStatus);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sort);
            model.addAttribute("sortDirection", direction);

            addCommonAttributes(model);

            log.info("Отображено {} персон на странице {}", processedPersons.getNumberOfElements(), page);
            return viewPrefix + "list";
        } catch (Exception e) {
            log.error("Ошибка при отображении списка персон: ", e);
            addErrorMessage(model, "Ошибка при загрузке списка: " + e.getMessage());
            addCommonAttributes(model);
            model.addAttribute(entityNamePlural, Page.empty());
            return viewPrefix + "list";
        }
    }

    /**
     * Поиск персон по возрасту.
     *
     * @param age возраст для поиска
     * @param page номер страницы
     * @param size размер страницы
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return имя шаблона или редирект
     */
    @GetMapping("/search/age/{age}")
    public String searchByAge(
            @PathVariable int age,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.debug("Поиск персон по возрасту: {}", age);

        try {
            if (age < 0 || age > 150) {
                addErrorMessage(redirectAttributes, "Некорректный возраст");
                return "redirect:" + basePath;
            }

            validatePaginationParams(page, size);
            Pageable pageable = PageRequest.of(page, size);

            // Упрощенная реализация - фильтруем в памяти
            List<T> allPersons = getService().findAll();
            List<T> filteredPersons = allPersons.stream()
                    .filter(person -> person.getBirthDate() != null &&
                            getService().isAdult(person.getBirthDate().plusYears(age)))
                    .collect(Collectors.toList());

            Page<T> persons = createPageFromList(filteredPersons, pageable);
            Page<T> processedPersons = postProcess(persons);

            model.addAttribute(entityNamePlural, processedPersons);
            model.addAttribute("searchAge", age);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            addCommonAttributes(model);

            log.info("Найдено {} персон по возрасту: {}", processedPersons.getNumberOfElements(), age);
            return viewPrefix + "list";
        } catch (Exception e) {
            log.error("Ошибка при поиске персон по возрасту {}: ", age, e);
            addErrorMessage(redirectAttributes, "Ошибка при поиске по возрасту: " + e.getMessage());
            return "redirect:" + basePath;
        }
    }

    /**
     * Поиск совершеннолетних персон.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param model модель для передачи данных в представление
     * @return имя шаблона
     */
    @GetMapping("/search/adults")
    public String searchAdults(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {

        log.debug("Поиск совершеннолетних персон");

        try {
            validatePaginationParams(page, size);
            Pageable pageable = PageRequest.of(page, size);

            List<T> allPersons = getService().findAll();
            List<T> adultPersons = allPersons.stream()
                    .filter(person -> person.getBirthDate() != null &&
                            getService().isAdult(person.getBirthDate()))
                    .collect(Collectors.toList());

            Page<T> persons = createPageFromList(adultPersons, pageable);
            Page<T> processedPersons = postProcess(persons);

            model.addAttribute(entityNamePlural, processedPersons);
            model.addAttribute("searchAdults", true);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            addCommonAttributes(model);

            log.info("Найдено {} совершеннолетних персон", processedPersons.getNumberOfElements());
            return viewPrefix + "list";
        } catch (Exception e) {
            log.error("Ошибка при поиске совершеннолетних персон: ", e);
            addErrorMessage(model, "Ошибка при поиске совершеннолетних: " + e.getMessage());
            addCommonAttributes(model);
            model.addAttribute(entityNamePlural, Page.empty());
            return viewPrefix + "list";
        }
    }

    /**
     * Экспорт данных в CSV.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона для экспорта
     */
    @GetMapping("/export/csv")
    public String exportToCsv(Model model) {
        log.debug("Экспорт персон в CSV");

        try {
            List<T> persons = getService().findAll();
            List<T> processedPersons = postProcess(persons);

            model.addAttribute(entityNamePlural, processedPersons);
            addCommonAttributes(model);

            log.info("Экспортировано {} персон в CSV", processedPersons.size());
            return viewPrefix + "export/csv";
        } catch (Exception e) {
            log.error("Ошибка при экспорте персон в CSV: ", e);
            addErrorMessage(model, "Ошибка при экспорте: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Импорт данных из CSV.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона для импорта
     */
    @GetMapping("/import/csv")
    public String importFromCsvForm(Model model) {
        log.debug("Отображение формы импорта персон из CSV");
        addCommonAttributes(model);
        return viewPrefix + "import/csv";
    }

    /**
     * Обработка импорта данных из CSV.
     *
     * @param csvContent содержимое CSV файла
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список
     */
    @PostMapping("/import/csv")
    public String importFromCsv(@RequestParam("csvContent") String csvContent,
                                RedirectAttributes redirectAttributes) {
        log.debug("Импорт персон из CSV");

        try {
            // Здесь должна быть реализация парсинга CSV
            // В базовой реализации просто показываем сообщение
            addSuccessMessage(redirectAttributes, "Импорт из CSV реализован в конкретных контроллерах");
            log.info("Импорт из CSV завершен");
            return "redirect:" + basePath;
        } catch (Exception e) {
            log.error("Ошибка при импорте персон из CSV: ", e);
            addErrorMessage(redirectAttributes, "Ошибка при импорте: " + e.getMessage());
            return "redirect:" + basePath + "/import/csv";
        }
    }

    /**
     * Просмотр статистики по персонам.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона статистики
     */
    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        log.debug("Отображение статистики по персонам");

        try {
            long totalPersons = getService().count();
            long maleCount = getService().countByGender(Gender.MALE);
            long femaleCount = getService().countByGender(Gender.FEMALE);
            long marriedCount = getService().countByMaritalStatus(MaritalStatus.MARRIED);
            long singleCount = getService().countByMaritalStatus(MaritalStatus.SINGLE);

            model.addAttribute("totalPersons", totalPersons);
            model.addAttribute("maleCount", maleCount);
            model.addAttribute("femaleCount", femaleCount);
            model.addAttribute("marriedCount", marriedCount);
            model.addAttribute("singleCount", singleCount);

            addCommonAttributes(model);

            log.info("Статистика по персонам отображена");
            return viewPrefix + "statistics";
        } catch (Exception e) {
            log.error("Ошибка при отображении статистики: ", e);
            addErrorMessage(model, "Ошибка при загрузке статистики: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
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

        return new PageImpl<>(pageContent, pageable, list.size());
    }

    /**
     * Получить список активных (неудалённых) сущностей.
     */
    @Override
    protected List<T> getActiveEntities() {
        return getService().findAll();
    }

    /**
     * Получить список удалённых сущностей.
     */
    @Override
    protected List<T> getDeletedEntities() {
        return getService().findAllDeleted();
    }

    /**
     * Получить список всех сущностей, включая удалённые.
     */
    @Override
    protected List<T> getAllEntities() {
        return getService().findAllWithDeleted();
    }

    /**
     * Постобработка сущности перед отображением.
     */
    @Override
    protected T postProcess(T entity) {
        return super.postProcess(entity);
    }

    /**
     * Постобработка списка сущностей перед отображением.
     */
    @Override
    protected List<T> postProcess(List<T> entities) {
        return super.postProcess(entities);
    }

    /**
     * Постобработка страницы сущностей перед отображением.
     */
    protected Page<T> postProcess(Page<T> page) {
        return page;
    }
}