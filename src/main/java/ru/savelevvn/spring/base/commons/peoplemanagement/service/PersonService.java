package ru.savelevvn.spring.base.commons.peoplemanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.savelevvn.spring.base.commons.BaseService;
import ru.savelevvn.spring.base.commons.peoplemanagement.Gender;
import ru.savelevvn.spring.base.commons.peoplemanagement.MaritalStatus;
import ru.savelevvn.spring.base.commons.peoplemanagement.Person;
import ru.savelevvn.spring.base.commons.peoplemanagement.repository.PersonRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Абстрактный базовый сервис для работы с персонами.
 * Предоставляет бизнес-логику для управления информацией о людях.
 *
 * <p>Реализует паттерн Template Method для гибкой настройки поведения
 * в конкретных реализациях сервисов.
 *
 * @param <T> тип сущности, расширяющей Person
 * @param <R> тип репозитория, расширяющего PersonRepository
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public abstract class PersonService<T extends Person, R extends PersonRepository<T>>
        extends BaseService<T, Long, R> {

    // Паттерны для валидации данных
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9. ()-]{10,25}$");
    private static final Pattern INN_PATTERN = Pattern.compile("^[0-9]{10,12}$");
    private static final Pattern SNILS_PATTERN = Pattern.compile("^[0-9]{11}$");
    private static final Pattern PASSPORT_SERIES_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final Pattern PASSPORT_NUMBER_PATTERN = Pattern.compile("^[0-9]{6}$");

    /**
     * Конструктор сервиса.
     *
     * @param repository репозиторий для работы с персонами
     */
    protected PersonService(R repository) {
        super(repository);
    }

    /**
     * Предобработка персоны перед сохранением.
     * Может быть переопределена в подклассах для реализации специфической логики.
     *
     * @param person персона для предобработки
     * @return обработанная персона
     */
    @Override
    protected T preSave(T person) {
        log.trace("Предобработка персоны перед сохранением: {}", person);

        // Форматирование номера телефона
        if (person.getPhone() != null) {
            person.setPhone(formatPhoneNumber(person.getPhone()));
        }

        // Нормализация email
        if (person.getEmail() != null) {
            person.setEmail(person.getEmail().toLowerCase().trim());
        }

        // Нормализация ФИО
        if (person.getFirstName() != null) {
            person.setFirstName(person.getFirstName().trim());
        }
        if (person.getLastName() != null) {
            person.setLastName(person.getLastName().trim());
        }
        if (person.getMiddleName() != null) {
            person.setMiddleName(person.getMiddleName().trim());
        }

        return person;
    }

    /**
     * Валидация персоны перед сохранением.
     * Может быть переопределена в подклассах для реализации бизнес-валидации.
     *
     * @param person персона для валидации
     * @throws IllegalArgumentException если персона не прошла валидацию
     */
    @Override
    protected void validate(T person) {
        log.trace("Валидация персоны: {}", person);
        validatePerson(person);
    }

    /**
     * Постобработка персоны после сохранения.
     * Может быть переопределена в подклассах для реализации дополнительной логики.
     *
     * @param person сохраненная персона
     * @return обработанная персона
     */
    @Override
    protected T postSave(T person) {
        log.trace("Постобработка персоны после сохранения: {}", person);
        return person;
    }

    /**
     * Создает новую персону с предварительной валидацией.
     *
     * @param person персона для создания
     * @return созданная персона
     * @throws IllegalArgumentException если персона не прошла валидацию
     */
    @Override
    @Transactional
    public T save(T person) {
        log.debug("Создание новой персоны: {}", person);
        return super.save(person);
    }

    /**
     * Создает новую персону (алиас для save).
     *
     * @param person персона для создания
     * @return созданная персона
     */
    @Transactional
    public T createPerson(T person) {
        log.debug("Создание персоны через createPerson: {}", person);
        return save(person);
    }

    /**
     * Обновляет информацию о существующей персоне.
     *
     * @param id идентификатор персоны
     * @param person обновленная информация о персоне
     * @return обновленная персона
     * @throws IllegalArgumentException если персона не найдена или не прошла валидацию
     */
    @Transactional
    public T updatePerson(Long id, T person) {
        log.debug("Обновление персоны с ID {}: {}", id, person);

        try {
            // Проверяем существование персоны
            if (!existsById(id)) {
                log.warn("Попытка обновления несуществующей персоны с ID: {}", id);
                throw new IllegalArgumentException("Персона с ID " + id + " не найдена");
            }

            // Устанавливаем ID для обновления
            person.setId(id);

            // Валидируем и сохраняем
            T updatedPerson = save(person);
            log.info("Персона успешно обновлена с ID: {}", id);

            return updatedPerson;
        } catch (Exception e) {
            log.error("Ошибка при обновлении персоны с ID {}: ", id, e);
            throw e;
        }
    }

    /**
     * Находит персону по email.
     *
     * @param email email для поиска
     * @return Optional с найденной персоной или пустой Optional
     */
    public Optional<T> findByEmail(String email) {
        log.debug("Поиск персоны по email: {}", email);
        if (email == null) {
            log.warn("Попытка поиска по null email");
            return Optional.empty();
        }
        Optional<T> person = getRepository().findByEmail(email.toLowerCase().trim());
        log.trace("Найдено {} персон по email: {}", person.isPresent() ? 1 : 0, email);
        return person;
    }

    /**
     * Находит персону по номеру телефона.
     *
     * @param phone номер телефона для поиска
     * @return Optional с найденной персоной или пустой Optional
     */
    public Optional<T> findByPhone(String phone) {
        log.debug("Поиск персоны по телефону: {}", phone);
        if (phone == null) {
            log.warn("Попытка поиска по null телефону");
            return Optional.empty();
        }
        Optional<T> person = getRepository().findByPhone(phone);
        log.trace("Найдено {} персон по телефону: {}", person.isPresent() ? 1 : 0, phone);
        return person;
    }

    /**
     * Находит всех персон с указанным именем.
     *
     * @param firstName имя для поиска
     * @return список персон с указанным именем
     */
    public List<T> findByFirstName(String firstName) {
        log.debug("Поиск персон по имени: {}", firstName);
        if (firstName == null) {
            log.warn("Попытка поиска по null имени");
            return List.of();
        }
        List<T> persons = getRepository().findByFirstName(firstName);
        log.trace("Найдено {} персон по имени: {}", persons.size(), firstName);
        return persons;
    }

    /**
     * Находит всех персон с указанным именем с пагинацией.
     *
     * @param firstName имя для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами с указанным именем
     */
    public Page<T> findByFirstName(String firstName, Pageable pageable) {
        log.debug("Поиск персон по имени с пагинацией: {}, страница: {}", firstName, pageable.getPageNumber());
        if (firstName == null) {
            log.warn("Попытка поиска по null имени");
            return Page.empty();
        }
        Page<T> persons = getRepository().findByFirstName(firstName, pageable);
        log.trace("Найдено {} персон по имени: {} на странице {}", persons.getNumberOfElements(), firstName, pageable.getPageNumber());
        return persons;
    }

    /**
     * Находит всех персон с указанной фамилией.
     *
     * @param lastName фамилия для поиска
     * @return список персон с указанной фамилией
     */
    public List<T> findByLastName(String lastName) {
        log.debug("Поиск персон по фамилии: {}", lastName);
        if (lastName == null) {
            log.warn("Попытка поиска по null фамилии");
            return List.of();
        }
        List<T> persons = getRepository().findByLastName(lastName);
        log.trace("Найдено {} персон по фамилии: {}", persons.size(), lastName);
        return persons;
    }

    /**
     * Находит всех персон с указанной фамилией с пагинацией.
     *
     * @param lastName фамилия для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами с указанной фамилией
     */
    public Page<T> findByLastName(String lastName, Pageable pageable) {
        log.debug("Поиск персон по фамилии с пагинацией: {}, страница: {}", lastName, pageable.getPageNumber());
        if (lastName == null) {
            log.warn("Попытка поиска по null фамилии");
            return Page.empty();
        }
        Page<T> persons = getRepository().findByLastName(lastName, pageable);
        log.trace("Найдено {} персон по фамилии: {} на странице {}", persons.getNumberOfElements(), lastName, pageable.getPageNumber());
        return persons;
    }

    /**
     * Находит всех персон с указанным именем и фамилией.
     *
     * @param firstName имя для поиска
     * @param lastName фамилия для поиска
     * @return список персон с указанными именем и фамилией
     */
    public List<T> findByFirstNameAndLastName(String firstName, String lastName) {
        log.debug("Поиск персон по имени и фамилии: {} {}", firstName, lastName);
        if (firstName == null || lastName == null) {
            log.warn("Попытка поиска по null имени или фамилии");
            return List.of();
        }
        List<T> persons = getRepository().findByFirstNameAndLastName(firstName, lastName);
        log.trace("Найдено {} персон по имени и фамилии: {} {}", persons.size(), firstName, lastName);
        return persons;
    }

    /**
     * Находит всех персон, родившихся в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список персон, родившихся в указанный период
     */
    public List<T> findByBirthDateBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Поиск персон по дате рождения между: {} и {}", startDate, endDate);
        if (startDate == null || endDate == null) {
            log.warn("Попытка поиска по null датам");
            return List.of();
        }
        List<T> persons = getRepository().findByBirthDateBetween(startDate, endDate);
        log.trace("Найдено {} персон по дате рождения между: {} и {}", persons.size(), startDate, endDate);
        return persons;
    }

    /**
     * Находит всех персон указанного пола.
     *
     * @param gender пол для фильтрации
     * @return список персон указанного пола
     */
    public List<T> findByGender(Gender gender) {
        log.debug("Поиск персон по полу: {}", gender);
        if (gender == null) {
            log.warn("Попытка поиска по null полу");
            return List.of();
        }
        List<T> persons = getRepository().findByGender(gender);
        log.trace("Найдено {} персон по полу: {}", persons.size(), gender);
        return persons;
    }

    /**
     * Находит всех персон с указанным семейным положением.
     *
     * @param maritalStatus семейное положение для фильтрации
     * @return список персон с указанным семейным положением
     */
    public List<T> findByMaritalStatus(MaritalStatus maritalStatus) {
        log.debug("Поиск персон по семейному положению: {}", maritalStatus);
        if (maritalStatus == null) {
            log.warn("Попытка поиска по null семейному положению");
            return List.of();
        }
        List<T> persons = getRepository().findByMaritalStatus(maritalStatus);
        log.trace("Найдено {} персон по семейному положению: {}", persons.size(), maritalStatus);
        return persons;
    }

    /**
     * Находит всех персон по номеру паспорта.
     *
     * @param passportNumber номер паспорта для поиска
     * @return список персон с указанным номером паспорта
     */
    public List<T> findByPassportNumber(String passportNumber) {
        log.debug("Поиск персон по номеру паспорта: {}", passportNumber);
        if (passportNumber == null) {
            log.warn("Попытка поиска по null номеру паспорта");
            return List.of();
        }
        List<T> persons = getRepository().findByPassportNumber(passportNumber);
        log.trace("Найдено {} персон по номеру паспорта: {}", persons.size(), passportNumber);
        return persons;
    }

    /**
     * Находит всех персон по ИНН.
     *
     * @param inn ИНН для поиска
     * @return список персон с указанным ИНН
     */
    public List<T> findByInn(String inn) {
        log.debug("Поиск персон по ИНН: {}", inn);
        if (inn == null) {
            log.warn("Попытка поиска по null ИНН");
            return List.of();
        }
        List<T> persons = getRepository().findByInn(inn);
        log.trace("Найдено {} персон по ИНН: {}", persons.size(), inn);
        return persons;
    }

    /**
     * Находит всех персон по СНИЛС.
     *
     * @param snils СНИЛС для поиска
     * @return список персон с указанным СНИЛС
     */
    public List<T> findBySnils(String snils) {
        log.debug("Поиск персон по СНИЛС: {}", snils);
        if (snils == null) {
            log.warn("Попытка поиска по null СНИЛС");
            return List.of();
        }
        List<T> persons = getRepository().findBySnils(snils);
        log.trace("Найдено {} персон по СНИЛС: {}", persons.size(), snils);
        return persons;
    }

    /**
     * Находит всех персон, чьи ФИО содержат указанный текст.
     *
     * @param text текст для поиска в ФИО
     * @return список персон, чьи ФИО содержат указанный текст
     */
    public List<T> findByFullNameContaining(String text) {
        log.debug("Поиск персон по ФИО, содержащему: {}", text);
        if (text == null || text.trim().isEmpty()) {
            log.warn("Попытка поиска по пустому тексту");
            return List.of();
        }
        List<T> persons = getRepository().findByFullNameContaining(text.trim());
        log.trace("Найдено {} персон по ФИО, содержащему: {}", persons.size(), text);
        return persons;
    }

    /**
     * Находит всех персон, чьи ФИО содержат указанный текст с пагинацией.
     *
     * @param text текст для поиска в ФИО
     * @param pageable параметры пагинации
     * @return страница с персонами, чьи ФИО содержат указанный текст
     */
    public Page<T> findByFullNameContaining(String text, Pageable pageable) {
        log.debug("Поиск персон по ФИО с пагинацией, содержащему: {}, страница: {}", text, pageable.getPageNumber());
        if (text == null || text.trim().isEmpty()) {
            log.warn("Попытка поиска по пустому тексту");
            return Page.empty();
        }
        Page<T> persons = getRepository().findByFullNameContaining(text.trim(), pageable);
        log.trace("Найдено {} персон по ФИО, содержащему: {} на странице {}", persons.getNumberOfElements(), text, pageable.getPageNumber());
        return persons;
    }

    /**
     * Находит персон по спецификации.
     *
     * @param spec спецификация для фильтрации
     * @return список персон, соответствующих спецификации
     */
    public List<T> findAll(Specification<T> spec) {
        log.debug("Поиск персон по спецификации");
        List<T> persons = getRepository().findAll(spec);
        log.trace("Найдено {} персон по спецификации", persons.size());
        return persons;
    }

    /**
     * Находит персон по спецификации с пагинацией.
     *
     * @param spec спецификация для фильтрации
     * @param pageable параметры пагинации
     * @return страница с персонами, соответствующими спецификации
     */
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        log.debug("Поиск персон по спецификации с пагинацией, страница: {}", pageable.getPageNumber());
        Page<T> persons = getRepository().findAll(spec, pageable);
        log.trace("Найдено {} персон по спецификации на странице {}", persons.getNumberOfElements(), pageable.getPageNumber());
        return persons;
    }

    /**
     * Проверяет существование персоны с указанным email.
     *
     * @param email email для проверки
     * @return true если персона с таким email существует, false в противном случае
     */
    public boolean existsByEmail(String email) {
        log.debug("Проверка существования персоны по email: {}", email);
        if (email == null) {
            log.warn("Попытка проверки существования по null email");
            return false;
        }
        boolean exists = getRepository().existsByEmail(email.toLowerCase().trim());
        log.trace("Персона с email {} {}", email, exists ? "существует" : "не существует");
        return exists;
    }

    /**
     * Проверяет существование персоны с указанным ИНН.
     *
     * @param inn ИНН для проверки
     * @return true если персона с таким ИНН существует, false в противном случае
     */
    public boolean existsByInn(String inn) {
        log.debug("Проверка существования персоны по ИНН: {}", inn);
        if (inn == null) {
            log.warn("Попытка проверки существования по null ИНН");
            return false;
        }
        boolean exists = getRepository().existsByInn(inn);
        log.trace("Персона с ИНН {} {}", inn, exists ? "существует" : "не существует");
        return exists;
    }

    /**
     * Проверяет существование персоны с указанным номером паспорта.
     *
     * @param passportNumber номер паспорта для проверки
     * @return true если персона с таким номером паспорта существует, false в противном случае
     */
    public boolean existsByPassportNumber(String passportNumber) {
        log.debug("Проверка существования персоны по номеру паспорта: {}", passportNumber);
        if (passportNumber == null) {
            log.warn("Попытка проверки существования по null номеру паспорта");
            return false;
        }
        boolean exists = getRepository().existsByPassportNumber(passportNumber);
        log.trace("Персона с номером паспорта {} {}", passportNumber, exists ? "существует" : "не существует");
        return exists;
    }

    /**
     * Валидирует персону перед сохранением.
     *
     * @param person персона для валидации
     * @throws IllegalArgumentException если персона не прошла валидацию
     */
    public void validatePerson(T person) {
        log.trace("Валидация персоны: {}", person);

        if (person == null) {
            throw new IllegalArgumentException("Персона не может быть null");
        }

        if (person.getFirstName() == null || person.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        if (person.getLastName() == null || person.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия не может быть пустой");
        }

        if (person.getEmail() != null && !person.getEmail().trim().isEmpty() && !isValidEmail(person.getEmail())) {
            throw new IllegalArgumentException("Некорректный формат email: " + person.getEmail());
        }

        if (person.getPhone() != null && !person.getPhone().trim().isEmpty() && !isValidPhoneNumber(person.getPhone())) {
            throw new IllegalArgumentException("Некорректный формат номера телефона: " + person.getPhone());
        }

        if (person.getInn() != null && !person.getInn().trim().isEmpty() && !isValidInn(person.getInn())) {
            throw new IllegalArgumentException("Некорректный формат ИНН: " + person.getInn());
        }

        if (person.getSnils() != null && !person.getSnils().trim().isEmpty() && !isValidSnils(person.getSnils())) {
            throw new IllegalArgumentException("Некорректный формат СНИЛС: " + person.getSnils());
        }

        if ((person.getPassportSeries() != null || person.getPassportNumber() != null) &&
                !isValidPassport(person.getPassportSeries(), person.getPassportNumber())) {
            throw new IllegalArgumentException("Некорректные серия или номер паспорта");
        }

        // Проверка даты рождения
        if (person.getBirthDate() != null && person.getBirthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }

        // Проверка уникальности email
        if (person.getEmail() != null && !person.getEmail().trim().isEmpty()) {
            Optional<T> existing = findByEmail(person.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(person.getId())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует: " + person.getEmail());
            }
        }

        // Проверка уникальности ИНН
        if (person.getInn() != null && !person.getInn().trim().isEmpty()) {
            List<T> existing = findByInn(person.getInn());
            if (!existing.isEmpty() && !existing.get(0).getId().equals(person.getId())) {
                throw new IllegalArgumentException("Пользователь с таким ИНН уже существует: " + person.getInn());
            }
        }

        // Проверка уникальности номера паспорта
        if (person.getPassportNumber() != null && !person.getPassportNumber().trim().isEmpty()) {
            List<T> existing = findByPassportNumber(person.getPassportNumber());
            if (!existing.isEmpty() && !existing.get(0).getId().equals(person.getId())) {
                throw new IllegalArgumentException("Пользователь с таким номером паспорта уже существует: " + person.getPassportNumber());
            }
        }
    }

    /**
     * Проверяет, достиг ли человек совершеннолетия.
     *
     * @param birthDate дата рождения
     * @return true если человеку 18 лет или больше, false в противном случае
     */
    public boolean isAdult(LocalDate birthDate) {
        if (birthDate == null) {
            log.warn("Попытка проверки совершеннолетия с null датой рождения");
            return false;
        }
        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        boolean adult = !birthDate.isAfter(eighteenYearsAgo);
        log.trace("Дата рождения {} соответствует совершеннолетию: {}", birthDate, adult);
        return adult;
    }

    /**
     * Проверяет, является ли человек пенсионером.
     *
     * @param birthDate дата рождения
     * @param gender пол
     * @return true если человек является пенсионером, false в противном случае
     */
    public boolean isPensioner(LocalDate birthDate, Gender gender) {
        if (birthDate == null || gender == null) {
            log.warn("Попытка проверки пенсионного возраста с null параметрами: дата={}, пол={}", birthDate, gender);
            return false;
        }

        int pensionAge = gender == Gender.MALE ? 65 : 60;
        LocalDate pensionDate = birthDate.plusYears(pensionAge);
        boolean pensioner = !pensionDate.isAfter(LocalDate.now());

        log.trace("Дата рождения {} и пол {} соответствуют пенсионному возрасту: {}", birthDate, gender, pensioner);
        return pensioner;
    }

    /**
     * Форматирует номер телефона в стандартный формат.
     *
     * @param phoneNumber номер телефона для форматирования
     * @return отформатированный номер телефона
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Форматируем в формате +7 (XXX) XXX-XX-XX
        if (cleanNumber.length() == 11 && (cleanNumber.startsWith("8") || cleanNumber.startsWith("7"))) {
            return "+7 (" + cleanNumber.substring(1, 4) + ") " + cleanNumber.substring(4, 7) +
                    "-" + cleanNumber.substring(7, 9) + "-" + cleanNumber.substring(9);
        } else if (cleanNumber.length() == 10) {
            return "+7 (" + cleanNumber.substring(0, 3) + ") " + cleanNumber.substring(3, 6) +
                    "-" + cleanNumber.substring(6, 8) + "-" + cleanNumber.substring(8);
        }

        // Если номер не соответствует ожидаемым форматам, возвращаем исходный
        log.debug("Номер телефона {} не соответствует стандартным форматам", phoneNumber);
        return phoneNumber;
    }

    /**
     * Проверяет валидность email.
     *
     * @param email email для проверки
     * @return true если email валидный, false в противном случае
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        boolean valid = EMAIL_PATTERN.matcher(email.trim()).matches();
        log.trace("Email {} валидность: {}", email, valid);
        return valid;
    }

    /**
     * Проверяет валидность номера телефона.
     *
     * @param phoneNumber номер телефона для проверки
     * @return true если номер телефона валидный, false в противном случае
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) return true; // Необязательное поле
        boolean valid = PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
        log.trace("Номер телефона {} валидность: {}", phoneNumber, valid);
        return valid;
    }

    /**
     * Проверяет валидность ИНН.
     *
     * @param inn ИНН для проверки
     * @return true если ИНН валидный, false в противном случае
     */
    public boolean isValidInn(String inn) {
        if (inn == null || inn.trim().isEmpty()) return true; // Необязательное поле
        boolean valid = INN_PATTERN.matcher(inn.trim()).matches();
        log.trace("ИНН {} валидность: {}", inn, valid);
        return valid;
    }

    /**
     * Проверяет валидность СНИЛС.
     *
     * @param snils СНИЛС для проверки
     * @return true если СНИЛС валидный, false в противном случае
     */
    public boolean isValidSnils(String snils) {
        if (snils == null || snils.trim().isEmpty()) return true; // Необязательное поле
        boolean valid = SNILS_PATTERN.matcher(snils.trim()).matches();
        log.trace("СНИЛС {} валидность: {}", snils, valid);
        return valid;
    }

    /**
     * Проверяет валидность серии и номера паспорта.
     *
     * @param series серия паспорта
     * @param number номер паспорта
     * @return true если серия и номер паспорта валидные, false в противном случае
     */
    public boolean isValidPassport(String series, String number) {
        // Если оба null - это валидно (паспорт не обязателен)
        if ((series == null || series.trim().isEmpty()) && (number == null || number.trim().isEmpty())) {
            return true;
        }

        // Если один из них задан, а другой нет - невалидно
        if ((series == null || series.trim().isEmpty()) || (number == null || number.trim().isEmpty())) {
            return false;
        }

        // Проверяем формат серии и номера
        boolean valid = PASSPORT_SERIES_PATTERN.matcher(series.trim()).matches() &&
                PASSPORT_NUMBER_PATTERN.matcher(number.trim()).matches();

        log.trace("Паспорт серия {} номер {} валидность: {}", series, number, valid);
        return valid;
    }

    /**
     * Получает количество персон по спецификации.
     *
     * @param spec спецификация для фильтрации
     * @return количество персон, соответствующих спецификации
     */
    public long count(Specification<T> spec) {
        log.debug("Подсчет персон по спецификации");
        long count = getRepository().count(spec);
        log.trace("Найдено {} персон по спецификации", count);
        return count;
    }

    /**
     * Получает всех совершеннолетних персон.
     *
     * @return список совершеннолетних персон
     */
    public List<T> findAdults() {
        log.debug("Поиск всех совершеннолетних персон");
        List<T> persons = getRepository().findAdults();
        log.trace("Найдено {} совершеннолетних персон", persons.size());
        return persons;
    }

    /**
     * Получает всех пенсионеров.
     *
     * @return список пенсионеров
     */
    public List<T> findPensioners() {
        log.debug("Поиск всех пенсионеров");
        List<T> persons = getRepository().findPensioners();
        log.trace("Найдено {} пенсионеров", persons.size());
        return persons;
    }

    /**
     * Получает персон с неполными данными.
     *
     * @return список персон с неполными данными
     */
    public List<T> findWithIncompleteData() {
        log.debug("Поиск персон с неполными данными");
        List<T> persons = getRepository().findWithIncompleteData();
        log.trace("Найдено {} персон с неполными данными", persons.size());
        return persons;
    }

    /**
     * Получает персон без контактной информации.
     *
     * @return список персон без контактной информации
     */
    public List<T> findWithoutContactInfo() {
        log.debug("Поиск персон без контактной информации");
        List<T> persons = getRepository().findWithoutContactInfo();
        log.trace("Найдено {} персон без контактной информации", persons.size());
        return persons;
    }

    /**
     * Подсчитывает количество персон указанного пола.
     *
     * @param gender пол для подсчета
     * @return количество персон указанного пола
     */
    public long countByGender(Gender gender) {
        log.debug("Подсчет персон по полу: {}", gender);
        if (gender == null) {
            log.warn("Попытка подсчета по null полу");
            return 0;
        }
        long count = getRepository().countByGender(gender);
        log.trace("Найдено {} персон по полу: {}", count, gender);
        return count;
    }

    /**
     * Подсчитывает количество персон с указанным семейным положением.
     *
     * @param maritalStatus семейное положение для подсчета
     * @return количество персон с указанным семейным положением
     */
    public long countByMaritalStatus(MaritalStatus maritalStatus) {
        log.debug("Подсчет персон по семейному положению: {}", maritalStatus);
        if (maritalStatus == null) {
            log.warn("Попытка подсчета по null семейному положению");
            return 0;
        }
        long count = getRepository().countByMaritalStatus(maritalStatus);
        log.trace("Найдено {} персон по семейному положению: {}", count, maritalStatus);
        return count;
    }
}