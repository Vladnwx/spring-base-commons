package ru.savelevvn.spring.base.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Базовый контроллер для Thymeleaf страниц.
 *
 * @param <T>  Тип сущности
 * @param <ID> Тип идентификатора
 * @param <S>  Тип сервиса
 */
@AllArgsConstructor
@Getter
public abstract class BaseThymeleafController<T extends BaseEntity<ID>, ID extends Serializable, S extends BaseService<T, ID, ?>> {

    private final S service;
    protected final String entityName; // Например: "user"
    protected final String entityNamePlural; // Например: "users"
    protected final String basePath; // Например: "/admin/users"
    protected final String viewPrefix; // Например: "admin/users/"

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

        model.addAttribute(entityNamePlural, entities);
        // Передаём значение фильтра в шаблон
        model.addAttribute("showDeleted", showDeletedStr);
        addCommonAttributes(model);
        // Передаём viewPrefix для использования специфичных фрагментов
        model.addAttribute("viewPrefix", viewPrefix);
        return viewPrefix + "list";
    }

    /**
     * Получить список активных (неудалённых) сущностей.
     * Реализация по умолчанию использует метод сервиса findAll().
     * Ваш сервис должен быть реализован так, чтобы findAll() возвращал только активные.
     * @return Список активных сущностей.
     */
    protected List<T> getActiveEntities() {
        return service.findAll(); // Предполагается, что findAll() возвращает активные
    }

    /**
     * Получить список удалённых сущностей.
     * Реализация по умолчанию использует метод сервиса findAllDeleted().
     * @return Список удалённых сущностей.
     */
    protected List<T> getDeletedEntities() {
        return service.findAllDeleted();
    }

    /**
     * Получить список всех сущностей, включая удалённые.
     * Реализация по умолчанию использует метод сервиса findAllWithDeleted().
     * @return Список всех сущностей.
     */
    protected List<T> getAllEntities() {
        return service.findAllWithDeleted();
    }

    /**
     * Форма для создания новой сущности
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        T entity = createNewEntity();
        model.addAttribute("entity", entity);
        model.addAttribute(entityName, entity);
        addCommonAttributes(model);
        model.addAttribute("viewPrefix", viewPrefix); // Для базового шаблона формы
        return viewPrefix + "form";
    }

    /**
     * Форма для редактирования сущности
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable ID id, Model model) {
        Optional<T> entityOpt = service.findById(id);
        if (entityOpt.isPresent()) {
            T entity = entityOpt.get();
            model.addAttribute("entity", entity);
            model.addAttribute(entityName, entity);
            addCommonAttributes(model);
            model.addAttribute("viewPrefix", viewPrefix); // Для базового шаблона формы
            return viewPrefix + "form";
        } else {
            return "redirect:" + basePath;
        }
    }

    /**
     * Просмотр деталей сущности
     */
    @GetMapping("/view/{id}")
    public String view(@PathVariable ID id, Model model) {
        Optional<T> entityOpt  = service.findById(id);
        if (entityOpt.isPresent()) {
            T entity = entityOpt.get();
            model.addAttribute("entity", entity);
            model.addAttribute(entityName, entity);
            addCommonAttributes(model);
            model.addAttribute("viewPrefix", viewPrefix); // Для базового шаблона просмотра
            return viewPrefix + "view";
        } else {
            return "redirect:" + basePath;
        }
    }

    /**
     * Сохранение сущности (создание или обновление)
     */
    @PostMapping("/save")
    public String save(@ModelAttribute T entity, RedirectAttributes redirectAttributes) {
        try {
            T savedEntity = service.save(entity);
            addSuccessMessage(redirectAttributes, "Entity saved successfully");
            return "redirect:" + basePath;
        } catch (Exception e) {
            addErrorMessage(redirectAttributes, "Error saving entity: " + e.getMessage());

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
        try {
            if (service.existsById(id)) {
                service.deleteById(id); // Soft-delete
                addSuccessMessage(redirectAttributes, "Entity deleted successfully");
            } else {
                addErrorMessage(redirectAttributes, "Entity not found");
            }
        } catch (Exception e) {
            addErrorMessage(redirectAttributes, "Error deleting entity: " + e.getMessage());
        }
        // После удаления возвращаемся к списку активных
        return "redirect:" + basePath;
    }

    /**
     * Восстановление удалённой сущности.
     */
    @PostMapping("/restore/{id}")
    public String restore(@PathVariable ID id, RedirectAttributes redirectAttributes) {
        try {
            // Проверяем существование (включая удаленные)
            if (service.findByIdWithDeleted(id).isPresent()) {
                service.restore(id); // Восстановление
                addSuccessMessage(redirectAttributes, "Entity restored successfully");
            } else {
                addErrorMessage(redirectAttributes, "Entity not found");
            }
        } catch (Exception e) {
            addErrorMessage(redirectAttributes, "Error restoring entity: " + e.getMessage());
        }
        // После восстановления возвращаемся к списку удалённых
        return "redirect:" + basePath + "?showDeleted=true";
    }

    /**
     * Создание новой сущности (должен быть реализован в подклассах)
     */
    protected abstract T createNewEntity();

    /**
     * Добавление общих атрибутов в модель
     */
    protected void addCommonAttributes(Model model) {
        model.addAttribute("basePath", basePath);
        model.addAttribute("entityName", entityName);
        model.addAttribute("entityNamePlural", entityNamePlural);
    }

    /**
     * Добавление сообщения об успехе
     */
    protected void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "success");
    }

    /**
     * Добавление сообщения об ошибке
     */
    protected void addErrorMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "error");
    }

    /**
     * Добавление предупреждающего сообщения
     */
    protected void addWarningMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "warning");
    }

    /**
     * Добавление информационного сообщения
     */
    protected void addInfoMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "info");
    }

    /**
     * Просмотр деталей УДАЛЕННОЙ сущности
     * Новый метод для работы с удаленными сущностями.
     */
    @GetMapping("/view-deleted/{id}") // Новый URL
    public String viewDeleted(@PathVariable ID id, Model model) {
        // Используем метод, который находит сущности, включая удаленные
        Optional<T> entityOpt  = service.findByIdWithDeleted(id);
        if (entityOpt.isPresent()) {
            T entity = entityOpt.get();
            // Дополнительная проверка, что сущность действительно удалена
            if (entity.isDeleted()) {
                model.addAttribute("entity", entity);
                model.addAttribute(entityName, entity);
                addCommonAttributes(model);
                model.addAttribute("viewPrefix", viewPrefix);
                // Можно использовать тот же шаблон view, или создать специальный
                return viewPrefix + "view";
            } else {
                // Если вдруг сущность не удалена, перенаправляем на обычный просмотр
                // Или на список, как в случае "не найдена"
                return "redirect:" + basePath + "/view/" + id;
            }
        } else {
            // Сущность не найдена даже с учетом удаленных
            return "redirect:" + basePath;
        }
    }

    /**
     * Форма для редактирования УДАЛЕННОЙ сущности
     * Новый метод для работы с удаленными сущностями.
     * Обычно редактирование удаленных сущностей запрещено,
     * но если нужно - используйте этот метод.
     */
    @GetMapping("/edit-deleted/{id}") // Новый URL
    public String editDeletedForm(@PathVariable ID id, Model model) {
        // Используем метод, который находит сущности, включая удаленные
        Optional<T> entityOpt = service.findByIdWithDeleted(id);
        if (entityOpt.isPresent()) {
            T entity = entityOpt.get();
            // Дополнительная проверка, что сущность действительно удалена
            if (entity.isDeleted()) {
                model.addAttribute("entity", entity);
                model.addAttribute(entityName, entity);
                addCommonAttributes(model);
                model.addAttribute("viewPrefix", viewPrefix);
                // Можно использовать тот же шаблон form, или создать специальный
                return viewPrefix + "form";
            } else {
                // Если вдруг сущность не удалена, перенаправляем на обычное редактирование
                // Или на список, как в случае "не найдена"
                return "redirect:" + basePath + "/edit/" + id;
            }
        } else {
            return "redirect:" + basePath;
        }
    }


}