package ru.savelevvn.spring.base.commons.peoplemanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.savelevvn.spring.base.commons.BaseService;
import ru.savelevvn.spring.base.commons.peoplemanagement.Person;
import ru.savelevvn.spring.base.commons.peoplemanagement.Gender;
import ru.savelevvn.spring.base.commons.peoplemanagement.MaritalStatus;
import ru.savelevvn.spring.base.commons.peoplemanagement.repository.PersonRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Абстрактный базовый сервис для работы с персонами.
 * Предоставляет бизнес-логику для управления информацией о людях.
 *
 * @param <T> тип сущности, расширяющей Person
 * @param <R> тип репозитория, расширяющего PersonRepository
 */
@Service
@Transactional
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
     * Создает новую персону с предварительной валидацией.
     *
     * @param person персона для создания
     * @return созданная персона
     * @throws IllegalArgumentException если персона не прошла валидацию
     */
    @Override
    @Transactional
    public T save(T person) {
        validatePerson(person);
        return super.save(person);
    }

    /**
     * Создает новую персону (алиас для save).
     *
     * @param person персона для создания
     * @return созданная персона
     */
    public T createPerson(T person) {
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
    public T updatePerson(Long id, T person) {
        // Проверяем существование персоны
        if (!existsById(id)) {
            throw new IllegalArgumentException("Персона с ID " + id + " не найдена");
        }

        // Устанавливаем ID для обновления
        person.setId(id);

        // Валидируем и сохраняем
        validatePerson(person);
        return save(person);
    }

    /**
     * Находит персону по email.
     *
     * @param email email для поиска
     * @return Optional с найденной персоной или пустой Optional
     */
    public Optional<T> findByEmail(String email) {
        return getRepository().findByEmail(email);
    }

    /**
     * Находит персону по номеру телефона.
     *
     * @param phone номер телефона для поиска
     * @return Optional с найденной персоной или пустой Optional
     */
    public Optional<T> findByPhone(String phone) {
        return getRepository().findByPhone(phone);
    }

    /**
     * Находит всех персон с указанным именем.
     *
     * @param firstName имя для поиска
     * @return список персон с указанным именем
     */
    public List<T> findByFirstName(String firstName) {
        return getRepository().findByFirstName(firstName);
    }

    /**
     * Находит всех персон с указанной фамилией.
     *
     * @param lastName фамилия для поиска
     * @return список персон с указанной фамилией
     */
    public List<T> findByLastName(String lastName) {
        return getRepository().findByLastName(lastName);
    }

    /**
     * Находит всех персон с указанным именем и фамилией.
     *
     * @param firstName имя для поиска
     * @param lastName фамилия для поиска
     * @return список персон с указанными именем и фамилией
     */
    public List<T> findByFirstNameAndLastName(String firstName, String lastName) {
        return getRepository().findByFirstNameAndLastName(firstName, lastName);
    }

    /**
     * Находит всех персон, родившихся в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список персон, родившихся в указанный период
     */
    public List<T> findByBirthDateBetween(LocalDate startDate, LocalDate endDate) {
        return getRepository().findByBirthDateBetween(startDate, endDate);
    }

    /**
     * Находит всех персон указанного пола.
     *
     * @param gender пол для фильтрации
     * @return список персон указанного пола
     */
    public List<T> findByGender(Gender gender) {
        return getRepository().findByGender(gender);
    }

    /**
     * Находит всех персон с указанным семейным положением.
     *
     * @param maritalStatus семейное положение для фильтрации
     * @return список персон с указанным семейным положением
     */
    public List<T> findByMaritalStatus(MaritalStatus maritalStatus) {
        return getRepository().findByMaritalStatus(maritalStatus);
    }

    /**
     * Находит всех персон по номеру паспорта.
     *
     * @param passportNumber номер паспорта для поиска
     * @return список персон с указанным номером паспорта
     */
    public List<T> findByPassportNumber(String passportNumber) {
        return getRepository().findByPassportNumber(passportNumber);
    }

    /**
     * Находит всех персон по ИНН.
     *
     * @param inn ИНН для поиска
     * @return список персон с указанным ИНН
     */
    public List<T> findByInn(String inn) {
        return getRepository().findByInn(inn);
    }

    /**
     * Находит всех персон по СНИЛС.
     *
     * @param snils СНИЛС для поиска
     * @return список персон с указанным СНИЛС
     */
    public List<T> findBySnils(String snils) {
        return getRepository().findBySnils(snils);
    }

    /**
     * Находит всех персон, чьи ФИО содержат указанный текст.
     *
     * @param text текст для поиска в ФИО
     * @return список персон, чьи ФИО содержат указанный текст
     */
    public List<T> findByFullNameContaining(String text) {
        return getRepository().findByFullNameContaining(text);
    }

    /**
     * Находит персон по спецификации.
     *
     * @param spec спецификация для фильтрации
     * @return список персон, соответствующих спецификации
     */
    public List<T> findAll(Specification<T> spec) {
        return getRepository().findAll(spec);
    }

    /**
     * Находит персон по спецификации с пагинацией.
     *
     * @param spec спецификация для фильтрации
     * @param pageable параметры пагинации
     * @return страница с персонами, соответствующими спецификации
     */
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return getRepository().findAll(spec, pageable);
    }

    /**
     * Проверяет существование персоны с указанным email.
     *
     * @param email email для проверки
     * @return true если персона с таким email существует, false в противном случае
     */
    public boolean existsByEmail(String email) {
        return getRepository().existsByEmail(email);
    }

    /**
     * Проверяет существование персоны с указанным ИНН.
     *
     * @param inn ИНН для проверки
     * @return true если персона с таким ИНН существует, false в противном случае
     */
    public boolean existsByInn(String inn) {
        return getRepository().existsByInn(inn);
    }

    /**
     * Проверяет существование персоны с указанным номером паспорта.
     *
     * @param passportNumber номер паспорта для проверки
     * @return true если персона с таким номером паспорта существует, false в противном случае
     */
    public boolean existsByPassportNumber(String passportNumber) {
        return getRepository().existsByPassportNumber(passportNumber);
    }

    /**
     * Валидирует персону перед сохранением.
     *
     * @param person персона для валидации
     * @throws IllegalArgumentException если персона не прошла валидацию
     */
    public void validatePerson(T person) {
        if (person.getFirstName() == null || person.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        if (person.getLastName() == null || person.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия не может быть пустой");
        }

        if (person.getEmail() != null && !isValidEmail(person.getEmail())) {
            throw new IllegalArgumentException("Некорректный формат email");
        }

        if (person.getPhone() != null && !isValidPhoneNumber(person.getPhone())) {
            throw new IllegalArgumentException("Некорректный формат номера телефона");
        }

        if (person.getInn() != null && !isValidInn(person.getInn())) {
            throw new IllegalArgumentException("Некорректный формат ИНН");
        }

        if (person.getSnils() != null && !isValidSnils(person.getSnils())) {
            throw new IllegalArgumentException("Некорректный формат СНИЛС");
        }

        if ((person.getPassportSeries() != null || person.getPassportNumber() != null) &&
                !isValidPassport(person.getPassportSeries(), person.getPassportNumber())) {
            throw new IllegalArgumentException("Некорректные серия или номер паспорта");
        }

        // Проверка уникальности email
        if (person.getEmail() != null) {
            Optional<T> existing = findByEmail(person.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(person.getId())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
        }

        // Проверка уникальности ИНН
        if (person.getInn() != null) {
            List<T> existing = findByInn(person.getInn());
            if (!existing.isEmpty() && !existing.get(0).getId().equals(person.getId())) {
                throw new IllegalArgumentException("Пользователь с таким ИНН уже существует");
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
        if (birthDate == null) return false;
        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        return birthDate.isBefore(eighteenYearsAgo) || birthDate.isEqual(eighteenYearsAgo);
    }

    /**
     * Форматирует номер телефона в стандартный формат.
     *
     * @param phoneNumber номер телефона для форматирования
     * @return отформатированный номер телефона
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Удаляем все нецифровые символы
        String digits = phoneNumber.replaceAll("[^0-9]", "");

        // Форматируем в формате +7 (XXX) XXX-XX-XX
        if (digits.length() == 11 && digits.startsWith("8")) {
            return "+7 (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) +
                    "-" + digits.substring(7, 9) + "-" + digits.substring(9);
        } else if (digits.length() == 11 && digits.startsWith("7")) {
            return "+7 (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) +
                    "-" + digits.substring(7, 9) + "-" + digits.substring(9);
        } else if (digits.length() == 10) {
            return "+7 (" + digits.substring(0, 3) + ") " + digits.substring(3, 6) +
                    "-" + digits.substring(6, 8) + "-" + digits.substring(8);
        }

        // Если номер не соответствует ожидаемым форматам, возвращаем исходный
        return phoneNumber;
    }

    /**
     * Проверяет валидность email.
     *
     * @param email email для проверки
     * @return true если email валидный, false в противном случае
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Проверяет валидность номера телефона.
     *
     * @param phoneNumber номер телефона для проверки
     * @return true если номер телефона валидный, false в противном случае
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return true; // Необязательное поле
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Проверяет валидность ИНН.
     *
     * @param inn ИНН для проверки
     * @return true если ИНН валидный, false в противном случае
     */
    public boolean isValidInn(String inn) {
        if (inn == null) return true; // Необязательное поле
        return INN_PATTERN.matcher(inn).matches();
    }

    /**
     * Проверяет валидность СНИЛС.
     *
     * @param snils СНИЛС для проверки
     * @return true если СНИЛС валидный, false в противном случае
     */
    public boolean isValidSnils(String snils) {
        if (snils == null) return true; // Необязательное поле
        return SNILS_PATTERN.matcher(snils).matches();
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
        if (series == null && number == null) return true;

        // Если один из них задан, а другой нет - невалидно
        if (series == null || number == null) return false;

        // Проверяем формат серии и номера
        return PASSPORT_SERIES_PATTERN.matcher(series).matches() &&
                PASSPORT_NUMBER_PATTERN.matcher(number).matches();
    }

    /**
     * Получает количество персон по спецификации.
     *
     * @param spec спецификация для фильтрации
     * @return количество персон, соответствующих спецификации
     */
    public long count(Specification<T> spec) {
        return getRepository().count(spec);
    }
}