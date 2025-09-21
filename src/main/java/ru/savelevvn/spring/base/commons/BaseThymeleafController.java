package ru.savelevvn.spring.base.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Базовый контроллер для Thymeleaf страниц с расширенной функциональностью.
 * Предоставляет стандартные CRUD операции для веб-интерфейса с поддержкой
 * сообщений, валидации и обработки ошибок.
 *
 * <p>Использует паттерн Template Method для гибкой настройки поведения
 * в конкретных реализациях контроллеров.
 *
 * @param <T>  Тип сущности, должен наследоваться от BaseEntity
 * @param <ID> Тип идентификатора сущности
 * @param <S>  Тип сервиса, должен наследоваться от BaseService
 * @version 1.0
 */
@Slf4j
@AllArgsConstructor
@Getter
public abstract class BaseThymeleafController<T extends BaseEntity<ID>, ID extends Serializable, S extends BaseService<T, ID, ?>> {

    protected final S service;
    protected final String entityName; // Например: "user"
    protected final String entityNamePlural; // Например: "users"
    protected final String basePath; // Например: "/admin/users"
    protected final String viewPrefix; // Например: "admin/users/"

    /**
     * Предобработка сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     *
     * @param entity сущность для предобработки
     * @return обработанная сущность
     */
    protected T preSave(T entity) {
        log.trace("Предобработка сущности перед сохранением: {}", entity);
        return entity;
    }

    /**
     * Предобработка сущности перед удалением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     *
     * @param entity сущность для предобработки
     * @return обработанная сущность
     */
    protected T preDelete(T entity) {
        log.trace("Предобработка сущности перед удалением: {}", entity);
        return entity;
    }

    /**
     * Валидация сущности перед сохранением.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если сущность не прошла валидацию
     */
    protected void validateSave(T entity) {
        log.trace("Валидация сущности перед сохранением: {}", entity);
        // Базовая валидация может быть реализована в подклассах
    }

    /**
     * Валидация идентификатора.
     * Может быть переопределена в подклассах для реализации специфической валидации.
     *
     * @param id идентификатор для валидации
     * @throws IllegalArgumentException если идентификатор не прошел валидацию
     */
    protected void validateId(ID id) {
        log.trace("Валидация идентификатора: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор не может быть null");
        }
    }

    /**
     * Постобработка сущности перед отображением.
     * Может быть переопределена в подклассах для реализации дополнительной логики.
     *
     * @param entity сущность для постобработки
     * @return обработанная сущность
     */
    protected T postProcess(T entity) {
        log.trace("Постобработка сущности перед отображением: {}", entity);
        return entity;
    }

    /**
     * Постобработка списка сущностей перед отображением.
     * Может быть переопределена в подклассах для реализации дополнительной логики.
     *
     * @param entities список сущностей для постобработки
     * @return обработанный список
     */
    protected List<T> postProcess(List<T> entities) {
        log.trace("Постобработка списка сущностей перед отображением, элементов: {}", entities.size());
        return entities;
    }

    /**
     * Обработка исключения.
     * Может быть переопределена в подклассах для реализации специфической обработки ошибок.
     *
     * @param e исключение для обработки
     * @param operation описание операции, в которой произошла ошибка
     * @param redirectAttributes атрибуты для редиректа
     */
    protected void handleException(Exception e, String operation, RedirectAttributes redirectAttributes) {
        log.error("Ошибка при выполнении операции '{}': ", operation, e);
        addErrorMessage(redirectAttributes, "Ошибка при " + operation + ": " + e.getMessage());
    }

    /**
     * Добавление общих атрибутов в модель.
     * Может быть переопределена в подклассах для добавления специфических атрибутов.
     *
     * @param model модель для добавления атрибутов
     */
    protected void addCommonAttributes(Model model) {
        model.addAttribute("basePath", basePath);
        model.addAttribute("entityName", entityName);
        model.addAttribute("entityNamePlural", entityNamePlural);
        model.addAttribute("viewPrefix", viewPrefix);
    }

    /**
     * Список сущностей с фильтрацией по статусу удаления.
     *
     * @param showDeletedStr Строка, определяющая фильтр: "true", "false", "all" или null.
     *                       - "true": показать только удалённые.
     *                       - "false": показать только активные (по умолчанию).
     *                       - "all": показать все.
     *                       - null: показать только активные (по умолчанию).
     * @param model Модель для передачи данных в представление.
     * @return Имя шаблона для отображения списка.
     */
    @RequestMapping
    public String list(@RequestParam(value = "showDeleted", required = false) String showDeletedStr, Model model) {
        log.debug("Отображение списка сущностей, фильтр: {}", showDeletedStr);

        try {
            List<T> entities;

            // Определяем, какие сущности нужно загрузить, в зависимости от параметра showDeleted
            if ("true".equalsIgnoreCase(showDeletedStr)) {
                entities = getDeletedEntities();
            } else if ("all".equalsIgnoreCase(showDeletedStr)) {
                entities = getAllEntities();
            } else {
                // По умолчанию или если showDeleted="false" - показываем только активные
                entities = getActiveEntities();
            }

            List<T> processedEntities = postProcess(entities);

            model.addAttribute(entityNamePlural, processedEntities);
            // Передаём значение фильтра в шаблон
            model.addAttribute("showDeleted", showDeletedStr);
            addCommonAttributes(model);

            log.info("Успешно загружено {} сущностей", processedEntities.size());
            return viewPrefix + "list";
        } catch (Exception e) {
            log.error("Ошибка при загрузке списка сущностей: ", e);
            addCommonAttributes(model);
            model.addAttribute("showDeleted", showDeletedStr);
            model.addAttribute(entityNamePlural, List.of());
            addErrorMessage(model, "Ошибка при загрузке списка: " + e.getMessage());
            return viewPrefix + "list";
        }
    }

    /**
     * Получить список активных (неудалённых) сущностей.
     * Реализация по умолчанию использует метод сервиса findAll().
     * Ваш сервис должен быть реализован так, чтобы findAll() возвращал только активные.
     * @return Список активных сущностей.
     */
    protected List<T> getActiveEntities() {
        log.trace("Получение активных сущностей");
        return service.findAll(); // Предполагается, что findAll() возвращает активные
    }

    /**
     * Получить список удалённых сущностей.
     * Реализация по умолчанию использует метод сервиса findAllDeleted().
     * @return Список удалённых сущностей.
     */
    protected List<T> getDeletedEntities() {
        log.trace("Получение удаленных сущностей");
        return service.findAllDeleted();
    }

    /**
     * Получить список всех сущностей, включая удалённые.
     * Реализация по умолчанию использует метод сервиса findAllWithDeleted().
     * @return Список всех сущностей.
     */
    protected List<T> getAllEntities() {
        log.trace("Получение всех сущностей");
        return service.findAllWithDeleted();
    }

    /**
     * Форма для создания новой сущности
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        log.debug("Отображение формы создания новой сущности");

        try {
            T entity = createNewEntity();
            model.addAttribute("entity", entity);
            model.addAttribute(entityName, entity);
            addCommonAttributes(model);
            return viewPrefix + "form";
        } catch (Exception e) {
            log.error("Ошибка при создании формы для новой сущности: ", e);
            addErrorMessage(model, "Ошибка при подготовке формы: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Форма для редактирования сущности
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable ID id, Model model) {
        log.debug("Отображение формы редактирования сущности с ID: {}", id);

        try {
            validateId(id);

            Optional<T> entityOpt = service.findById(id);
            if (entityOpt.isPresent()) {
                T entity = postProcess(entityOpt.get());
                model.addAttribute("entity", entity);
                model.addAttribute(entityName, entity);
                addCommonAttributes(model);
                return viewPrefix + "form";
            } else {
                log.info("Сущность не найдена при редактировании, ID: {}", id);
                addErrorMessage(model, "Сущность не найдена");
                addCommonAttributes(model);
                return "redirect:" + basePath;
            }
        } catch (Exception e) {
            log.error("Ошибка при создании формы редактирования для ID {}: ", id, e);
            addErrorMessage(model, "Ошибка при подготовке формы редактирования: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Просмотр деталей сущности
     */
    @GetMapping("/view/{id}")
    public String view(@PathVariable ID id, Model model) {
        log.debug("Просмотр деталей сущности с ID: {}", id);

        try {
            validateId(id);

            Optional<T> entityOpt = service.findById(id);
            if (entityOpt.isPresent()) {
                T entity = postProcess(entityOpt.get());
                model.addAttribute("entity", entity);
                model.addAttribute(entityName, entity);
                addCommonAttributes(model);
                return viewPrefix + "view";
            } else {
                log.info("Сущность не найдена при просмотре, ID: {}", id);
                addErrorMessage(model, "Сущность не найдена");
                addCommonAttributes(model);
                return "redirect:" + basePath;
            }
        } catch (Exception e) {
            log.error("Ошибка при просмотре сущности с ID {}: ", id, e);
            addErrorMessage(model, "Ошибка при просмотре сущности: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Сохранение сущности (создание или обновление)
     */
    @PostMapping("/save")
    public String save(@ModelAttribute T entity, RedirectAttributes redirectAttributes) {
        log.debug("Сохранение сущности: {}", entity);

        try {
            // Валидация
            validateSave(entity);

            // Предобработка
            T processedEntity = preSave(entity);

            // Сохранение
            T savedEntity = service.save(processedEntity);

            log.info("Сущность успешно сохранена с ID: {}", savedEntity.getId());
            addSuccessMessage(redirectAttributes, "Сущность успешно сохранена");
            return "redirect:" + basePath;
        } catch (Exception e) {
            handleException(e, "сохранении сущности", redirectAttributes);

            // Для новой сущности редирект на форму создания
            if (entity.getId() == null) {
                return "redirect:" + basePath + "/new";
            }
            // Для существующей - на форму редактирования
            else {
                return "redirect:" + basePath + "/edit/" + entity.getId();
            }
        }
    }

    /**
     * Удаление сущности (soft-delete)
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable ID id, RedirectAttributes redirectAttributes) {
        log.debug("Удаление сущности с ID: {}", id);

        try {
            validateId(id);

            if (service.existsById(id)) {
                Optional<T> entityOpt = service.findById(id);
                if (entityOpt.isPresent()) {
                    T entity = preDelete(entityOpt.get());
                    service.delete(entity); // Soft-delete
                } else {
                    service.deleteById(id);
                }
                log.info("Сущность успешно удалена (soft-delete) с ID: {}", id);
                addSuccessMessage(redirectAttributes, "Сущность успешно удалена");
            } else {
                log.info("Попытка удаления несуществующей сущности с ID: {}", id);
                addErrorMessage(redirectAttributes, "Сущность не найдена");
            }
        } catch (Exception e) {
            handleException(e, "удалении сущности", redirectAttributes);
        }
        // После удаления возвращаемся к списку активных
        return "redirect:" + basePath;
    }

    /**
     * Восстановление удалённой сущности.
     */
    @PostMapping("/restore/{id}")
    public String restore(@PathVariable ID id, RedirectAttributes redirectAttributes) {
        log.debug("Восстановление сущности с ID: {}", id);

        try {
            validateId(id);

            // Проверяем существование (включая удаленные)
            Optional<T> entityOpt = service.findByIdWithDeleted(id);
            if (entityOpt.isPresent()) {
                T entity = entityOpt.get();
                if (entity.isDeleted()) {
                    service.restore(id); // Восстановление
                    log.info("Сущность успешно восстановлена с ID: {}", id);
                    addSuccessMessage(redirectAttributes, "Сущность успешно восстановлена");
                } else {
                    log.info("Попытка восстановления неудаленной сущности с ID: {}", id);
                    addErrorMessage(redirectAttributes, "Сущность не была удалена");
                }
            } else {
                log.info("Сущность не найдена при восстановлении, ID: {}", id);
                addErrorMessage(redirectAttributes, "Сущность не найдена");
            }
        } catch (Exception e) {
            handleException(e, "восстановлении сущности", redirectAttributes);
        }
        // После восстановления возвращаемся к списку удалённых
        return "redirect:" + basePath + "?showDeleted=true";
    }

    /**
     * Создание новой сущности (должен быть реализован в подклассах)
     */
    protected abstract T createNewEntity();

    /**
     * Добавление сообщения об успехе в RedirectAttributes
     */
    protected void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "success");
    }

    /**
     * Добавление сообщения об ошибке в RedirectAttributes
     */
    protected void addErrorMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "error");
    }

    /**
     * Добавление предупреждающего сообщения в RedirectAttributes
     */
    protected void addWarningMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "warning");
    }

    /**
     * Добавление информационного сообщения в RedirectAttributes
     */
    protected void addInfoMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "info");
    }

    /**
     * Добавление сообщения об успехе в Model (для отображения на странице)
     */
    protected void addSuccessMessage(Model model, String message) {
        model.addAttribute("message", message);
        model.addAttribute("messageType", "success");
    }

    /**
     * Добавление сообщения об ошибке в Model (для отображения на странице)
     */
    protected void addErrorMessage(Model model, String message) {
        model.addAttribute("message", message);
        model.addAttribute("messageType", "error");
    }

    /**
     * Добавление предупреждающего сообщения в Model (для отображения на странице)
     */
    protected void addWarningMessage(Model model, String message) {
        model.addAttribute("message", message);
        model.addAttribute("messageType", "warning");
    }

    /**
     * Добавление информационного сообщения в Model (для отображения на странице)
     */
    protected void addInfoMessage(Model model, String message) {
        model.addAttribute("message", message);
        model.addAttribute("messageType", "info");
    }

    /**
     * Просмотр деталей УДАЛЕННОЙ сущности
     * Новый метод для работы с удаленными сущностями.
     */
    @GetMapping("/view-deleted/{id}")
    public String viewDeleted(@PathVariable ID id, Model model) {
        log.debug("Просмотр деталей удаленной сущности с ID: {}", id);

        try {
            validateId(id);

            // Используем метод, который находит сущности, включая удаленные
            Optional<T> entityOpt = service.findByIdWithDeleted(id);
            if (entityOpt.isPresent()) {
                T entity = entityOpt.get();
                // Дополнительная проверка, что сущность действительно удалена
                if (entity.isDeleted()) {
                    T processedEntity = postProcess(entity);
                    model.addAttribute("entity", processedEntity);
                    model.addAttribute(entityName, processedEntity);
                    addCommonAttributes(model);
                    return viewPrefix + "view";
                } else {
                    // Если вдруг сущность не удалена, перенаправляем на обычный просмотр
                    return "redirect:" + basePath + "/view/" + id;
                }
            } else {
                // Сущность не найдена даже с учетом удаленных
                log.info("Удаленная сущность не найдена, ID: {}", id);
                addErrorMessage(model, "Удаленная сущность не найдена");
                addCommonAttributes(model);
                return "redirect:" + basePath;
            }
        } catch (Exception e) {
            log.error("Ошибка при просмотре удаленной сущности с ID {}: ", id, e);
            addErrorMessage(model, "Ошибка при просмотре удаленной сущности: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }

    /**
     * Форма для редактирования УДАЛЕННОЙ сущности
     * Новый метод для работы с удаленными сущностями.
     * Обычно редактирование удаленных сущностей запрещено,
     * но если нужно - используйте этот метод.
     */
    @GetMapping("/edit-deleted/{id}")
    public String editDeletedForm(@PathVariable ID id, Model model) {
        log.debug("Отображение формы редактирования удаленной сущности с ID: {}", id);

        try {
            validateId(id);

            // Используем метод, который находит сущности, включая удаленные
            Optional<T> entityOpt = service.findByIdWithDeleted(id);
            if (entityOpt.isPresent()) {
                T entity = entityOpt.get();
                // Дополнительная проверка, что сущность действительно удалена
                if (entity.isDeleted()) {
                    T processedEntity = postProcess(entity);
                    model.addAttribute("entity", processedEntity);
                    model.addAttribute(entityName, processedEntity);
                    addCommonAttributes(model);
                    return viewPrefix + "form";
                } else {
                    // Если вдруг сущность не удалена, перенаправляем на обычное редактирование
                    return "redirect:" + basePath + "/edit/" + id;
                }
            } else {
                log.info("Удаленная сущность не найдена при редактировании, ID: {}", id);
                addErrorMessage(model, "Удаленная сущность не найдена");
                addCommonAttributes(model);
                return "redirect:" + basePath;
            }
        } catch (Exception e) {
            log.error("Ошибка при создании формы редактирования удаленной сущности с ID {}: ", id, e);
            addErrorMessage(model, "Ошибка при подготовке формы редактирования удаленной сущности: " + e.getMessage());
            addCommonAttributes(model);
            return "redirect:" + basePath;
        }
    }
}