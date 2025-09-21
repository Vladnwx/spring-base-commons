package ru.savelevvn.spring.base.commons.peoplemanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.savelevvn.spring.base.commons.BaseController;
import ru.savelevvn.spring.base.commons.peoplemanagement.Gender;
import ru.savelevvn.spring.base.commons.peoplemanagement.MaritalStatus;
import ru.savelevvn.spring.base.commons.peoplemanagement.Person;
import ru.savelevvn.spring.base.commons.peoplemanagement.service.PersonService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Базовый REST контроллер для работы с персонами.
 * Предоставляет стандартные CRUD операции и расширенные методы поиска.
 *
 * @param <T> тип сущности, расширяющей Person
 * @param <S> тип сервиса, расширяющего PersonService
 * @version 1.0
 */
@Slf4j
public abstract class PersonController<T extends Person, S extends PersonService<T, ?>>
        extends BaseController<T, Long, S> {

    /**
     * Конструктор контроллера.
     *
     * @param service сервис для работы с персонами
     */
    protected PersonController(S service) {
        super(service);
    }

    /**
     * Поиск персон по email.
     *
     * @param email email для поиска
     * @return найденная персона или 404 если не найдена
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<T> getByEmail(@PathVariable String email) {
        log.debug("Поиск персоны по email: {}", email);

        try {
            Optional<T> person = getService().findByEmail(email);
            if (person.isPresent()) {
                log.info("Персона найдена по email: {}", email);
                T processedPerson = getPostProcess().apply(person.get());
                return ResponseEntity.ok(processedPerson);
            } else {
                log.info("Персона не найдена по email: {}", email);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при поиске персоны по email {}: ", email, e);
            return (ResponseEntity<T>) handleException(e, "поиск персоны по email " + email);
        }
    }

    /**
     * Поиск персон по номеру телефона.
     *
     * @param phone номер телефона для поиска
     * @return найденная персона или 404 если не найдена
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<T> getByPhone(@PathVariable String phone) {
        log.debug("Поиск персоны по телефону: {}", phone);

        try {
            Optional<T> person = getService().findByPhone(phone);
            if (person.isPresent()) {
                log.info("Персона найдена по телефону: {}", phone);
                T processedPerson = getPostProcess().apply(person.get());
                return ResponseEntity.ok(processedPerson);
            } else {
                log.info("Персона не найдена по телефону: {}", phone);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при поиске персоны по телефону {}: ", phone, e);
            return (ResponseEntity<T>) handleException(e, "поиск персоны по телефону " + phone);
        }
    }

    /**
     * Поиск персон по имени с пагинацией.
     *
     * @param firstName имя для поиска
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с персонами
     */
    @GetMapping("/search/firstName/{firstName}")
    public ResponseEntity<Page<T>> searchByFirstName(
            @PathVariable String firstName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск персон по имени: {}, страница={}, размер={}", firstName, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            // Получаем все результаты и создаем страницу вручную
            List<T> allPersons = getService().findByFirstName(firstName);
            Page<T> persons = createPageFromList(allPersons, pageable);
            Page<T> processedPersons = getPostProcessPage().apply(persons);

            log.info("Найдено {} персон по имени: {}", persons.getNumberOfElements(), firstName);
            return ResponseEntity.ok(processedPersons);
        } catch (Exception e) {
            log.error("Ошибка при поиске персон по имени {}: ", firstName, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск персон по имени " + firstName);
        }
    }

    /**
     * Поиск персон по фамилии с пагинацией.
     *
     * @param lastName фамилия для поиска
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с персонами
     */
    @GetMapping("/search/lastName/{lastName}")
    public ResponseEntity<Page<T>> searchByLastName(
            @PathVariable String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск персон по фамилии: {}, страница={}, размер={}", lastName, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            // Получаем все результаты и создаем страницу вручную
            List<T> allPersons = getService().findByLastName(lastName);
            Page<T> persons = createPageFromList(allPersons, pageable);
            Page<T> processedPersons = getPostProcessPage().apply(persons);

            log.info("Найдено {} персон по фамилии: {}", persons.getNumberOfElements(), lastName);
            return ResponseEntity.ok(processedPersons);
        } catch (Exception e) {
            log.error("Ошибка при поиске персон по фамилии {}: ", lastName, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск персон по фамилии " + lastName);
        }
    }

    /**
     * Поиск персон по ФИО с пагинацией.
     *
     * @param text текст для поиска в ФИО
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с персонами
     */
    @GetMapping("/search/fullName")
    public ResponseEntity<Page<T>> searchByFullName(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск персон по ФИО, содержащему: {}, страница={}, размер={}", text, page, size);

        try {
            if (text == null || text.trim().isEmpty()) {
                log.warn("Попытка поиска по пустому тексту");
                return ResponseEntity.badRequest().build();
            }

            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            // Получаем все результаты и создаем страницу вручную
            List<T> allPersons = getService().findByFullNameContaining(text);
            Page<T> persons = createPageFromList(allPersons, pageable);
            Page<T> processedPersons = getPostProcessPage().apply(persons);

            log.info("Найдено {} персон по ФИО, содержащему: {}", persons.getNumberOfElements(), text);
            return ResponseEntity.ok(processedPersons);
        } catch (Exception e) {
            log.error("Ошибка при поиске персон по ФИО {}: ", text, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск персон по ФИО " + text);
        }
    }

    /**
     * Поиск персон по полу.
     *
     * @param gender пол для фильтрации
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с персонами
     */
    @GetMapping("/search/gender/{gender}")
    public ResponseEntity<Page<T>> searchByGender(
            @PathVariable Gender gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск персон по полу: {}, страница={}, размер={}", gender, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            // Получаем все результаты и создаем страницу вручную
            List<T> allPersons = getService().findByGender(gender);
            Page<T> persons = createPageFromList(allPersons, pageable);
            Page<T> processedPersons = getPostProcessPage().apply(persons);

            log.info("Найдено {} персон по полу: {}", persons.getNumberOfElements(), gender);
            return ResponseEntity.ok(processedPersons);
        } catch (Exception e) {
            log.error("Ошибка при поиске персон по полу {}: ", gender, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск персон по полу " + gender);
        }
    }

    /**
     * Поиск персон по семейному положению.
     *
     * @param maritalStatus семейное положение для фильтрации
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param direction направление сортировки
     * @return страница с персонами
     */
    @GetMapping("/search/maritalStatus/{maritalStatus}")
    public ResponseEntity<Page<T>> searchByMaritalStatus(
            @PathVariable MaritalStatus maritalStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Поиск персон по семейному положению: {}, страница={}, размер={}", maritalStatus, page, size);

        try {
            validatePaginationParams(page, size);
            Pageable pageable = createPageable(page, size, sort, direction);

            // Получаем все результаты и создаем страницу вручную
            List<T> allPersons = getService().findByMaritalStatus(maritalStatus);
            Page<T> persons = createPageFromList(allPersons, pageable);
            Page<T> processedPersons = getPostProcessPage().apply(persons);

            log.info("Найдено {} персон по семейному положению: {}", persons.getNumberOfElements(), maritalStatus);
            return ResponseEntity.ok(processedPersons);
        } catch (Exception e) {
            log.error("Ошибка при поиске персон по семейному положению {}: ", maritalStatus, e);
            return (ResponseEntity<Page<T>>) handleException(e, "поиск персон по семейному положению " + maritalStatus);
        }
    }

    /**
     * Поиск персон по дате рождения.
     *
     * @param birthDate дата рождения для поиска
     * @return список персон с указанной датой рождения
     */
    @GetMapping("/search/birthDate/{birthDate}")
    public ResponseEntity<List<T>> searchByBirthDate(@PathVariable LocalDate birthDate) {
        log.debug("Поиск персон по дате рождения: {}", birthDate);

        try {
            List<T> persons = getService().findByBirthDateBetween(birthDate, birthDate);
            List<T> processedPersons = persons.stream()
                    .map(getPostProcess())
                    .collect(Collectors.toList());

            log.info("Найдено {} персон по дате рождения: {}", processedPersons.size(), birthDate);
            return ResponseEntity.ok(processedPersons);
        } catch (Exception e) {
            log.error("Ошибка при поиске персон по дате рождения {}: ", birthDate, e);
            return (ResponseEntity<List<T>>) handleException(e, "поиск персон по дате рождения " + birthDate);
        }
    }

    /**
     * Обновить персону.
     *
     * @param id идентификатор персоны
     * @param person обновленные данные персоны
     * @return обновленная персона
     */
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<T> update(@PathVariable Long id, @Valid @RequestBody T person) {
        log.debug("Обновление персоны с ID {}: {}", id, person);

        try {
            validateId(id);

            if (!getService().existsById(id)) {
                log.info("Попытка обновления несуществующей персоны с ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Валидация
            getService().validatePerson(person);

            // Обновление
            T updatedEntity = getService().updatePerson(id, person);

            // Постобработка
            T resultEntity = getPostProcess().apply(updatedEntity);

            log.info("Персона успешно обновлена с ID: {}", id);
            return ResponseEntity.ok(resultEntity);
        } catch (Exception e) {
            log.error("Ошибка при обновлении персоны с ID {}: ", id, e);
            return (ResponseEntity<T>) handleException(e, "обновление персоны с ID " + id);
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
     * Создание объекта Pageable.
     */
    protected Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        return PageRequest.of(page, size, Sort.by(sortDirection, sort));
    }

    /**
     * Обработка исключения.
     */
    @SuppressWarnings("unchecked")
    protected ResponseEntity<?> handleException(Exception e, String operation) {
        log.error("Ошибка при выполнении операции '{}': ", operation, e);
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Функция предобработки сущности.
     */
    protected Function<T, T> getPreSave() {
        return entity -> entity;
    }

    /**
     * Функция постобработки сущности.
     */
    protected Function<T, T> getPostProcess() {
        return entity -> entity;
    }

    /**
     * Функция постобработки страницы.
     */
    @SuppressWarnings("unchecked")
    protected Function<Page<T>, Page<T>> getPostProcessPage() {
        return (Function<Page<T>, Page<T>>) (Function<?, ?>) page -> page;
    }

    /**
     * Валидация идентификатора.
     */
    protected void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор не может быть null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Идентификатор должен быть положительным числом");
        }
    }
}