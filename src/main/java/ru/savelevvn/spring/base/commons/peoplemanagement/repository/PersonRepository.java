package ru.savelevvn.spring.base.commons.peoplemanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.savelevvn.spring.base.commons.BaseRepository;
import ru.savelevvn.spring.base.commons.peoplemanagement.Gender;
import ru.savelevvn.spring.base.commons.peoplemanagement.MaritalStatus;
import ru.savelevvn.spring.base.commons.peoplemanagement.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Базовый репозиторий для работы с сущностями типа Person.
 * Расширяет BaseRepository и JpaSpecificationExecutor для динамических запросов.
 *
 * <p>Предоставляет общие методы для поиска и фильтрации персон по различным критериям:
 * <ul>
 *   <li>Поиск по ФИО</li>
 *   <li>Поиск по контактной информации</li>
 *   <li>Фильтрация по дате рождения</li>
 *   <li>Фильтрация по полу и семейному положению</li>
 *   <li>Пагинация и сортировка результатов</li>
 * </ul>
 *
 * <p>Использует JPA Specification для динамического построения запросов.
 *
 * @param <T> тип сущности, расширяющей Person
 * @author Savelev Vladimir
 * @version 1.0
 * @since 1.0
 * @see Person
 * @see BaseRepository
 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor
 */
public interface PersonRepository<T extends Person>
        extends BaseRepository<T, Long>, JpaSpecificationExecutor<T> {

    /**
     * Находит персону по email.
     *
     * @param email email для поиска
     * @return Optional с найденной персоной или пустой Optional
     */
    Optional<T> findByEmail(String email);

    /**
     * Находит персону по email (включая удаленные).
     *
     * @param email email для поиска
     * @return Optional с найденной персоной или пустой Optional
     */
    @Query("SELECT p FROM #{#entityName} p WHERE p.email = :email")
    Optional<T> findByEmailWithDeleted(@Param("email") String email);

    /**
     * Находит персону по номеру телефона.
     *
     * @param phone номер телефона для поиска
     * @return Optional с найденной персоной или пустой Optional
     */
    Optional<T> findByPhone(String phone);

    /**
     * Находит всех персон с указанным именем.
     *
     * @param firstName имя для поиска
     * @return список персон с указанным именем
     */
    List<T> findByFirstName(String firstName);

    /**
     * Находит всех персон с указанным именем с пагинацией.
     *
     * @param firstName имя для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами с указанным именем
     */
    Page<T> findByFirstName(String firstName, Pageable pageable);

    /**
     * Находит всех персон с указанной фамилией.
     *
     * @param lastName фамилия для поиска
     * @return список персон с указанной фамилией
     */
    List<T> findByLastName(String lastName);

    /**
     * Находит всех персон с указанной фамилией с пагинацией.
     *
     * @param lastName фамилия для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами с указанной фамилией
     */
    Page<T> findByLastName(String lastName, Pageable pageable);

    /**
     * Находит всех персон с указанным именем и фамилией.
     *
     * @param firstName имя для поиска
     * @param lastName фамилия для поиска
     * @return список персон с указанными именем и фамилией
     */
    List<T> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Находит всех персон с указанным именем и фамилией с пагинацией.
     *
     * @param firstName имя для поиска
     * @param lastName фамилия для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами с указанными именем и фамилией
     */
    Page<T> findByFirstNameAndLastName(String firstName, String lastName, Pageable pageable);

    /**
     * Находит всех персон с указанной датой рождения.
     *
     * @param birthDate дата рождения для поиска
     * @return список персон с указанной датой рождения
     */
    List<T> findByBirthDate(LocalDate birthDate);

    /**
     * Находит всех персон с указанной датой рождения с пагинацией.
     *
     * @param birthDate дата рождения для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами с указанной датой рождения
     */
    Page<T> findByBirthDate(LocalDate birthDate, Pageable pageable);

    /**
     * Находит всех персон, родившихся в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список персон, родившихся в указанный период
     */
    List<T> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Находит всех персон, родившихся в указанный период с пагинацией.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @param pageable параметры пагинации
     * @return страница с персонами, родившимися в указанный период
     */
    Page<T> findByBirthDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Находит всех персон указанного пола.
     *
     * @param gender пол для фильтрации
     * @return список персон указанного пола
     */
    List<T> findByGender(Gender gender);

    /**
     * Находит всех персон указанного пола с пагинацией.
     *
     * @param gender пол для фильтрации
     * @param pageable параметры пагинации
     * @return страница с персонами указанного пола
     */
    Page<T> findByGender(Gender gender, Pageable pageable);

    /**
     * Находит всех персон с указанным семейным положением.
     *
     * @param maritalStatus семейное положение для фильтрации
     * @return список персон с указанным семейным положением
     */
    List<T> findByMaritalStatus(MaritalStatus maritalStatus);

    /**
     * Находит всех персон с указанным семейным положением с пагинацией.
     *
     * @param maritalStatus семейное положение для фильтрации
     * @param pageable параметры пагинации
     * @return страница с персонами с указанным семейным положением
     */
    Page<T> findByMaritalStatus(MaritalStatus maritalStatus, Pageable pageable);

    /**
     * Находит всех персон по номеру паспорта.
     *
     * @param passportNumber номер паспорта для поиска
     * @return список персон с указанным номером паспорта
     */
    List<T> findByPassportNumber(String passportNumber);

    /**
     * Находит всех персон по серии и номеру паспорта.
     *
     * @param passportSeries серия паспорта
     * @param passportNumber номер паспорта
     * @return список персон с указанными серией и номером паспорта
     */
    List<T> findByPassportSeriesAndPassportNumber(String passportSeries, String passportNumber);

    /**
     * Находит всех персон по ИНН.
     *
     * @param inn ИНН для поиска
     * @return список персон с указанным ИНН
     */
    List<T> findByInn(String inn);

    /**
     * Находит всех персон по СНИЛС.
     *
     * @param snils СНИЛС для поиска
     * @return список персон с указанным СНИЛС
     */
    List<T> findBySnils(String snils);

    /**
     * Находит всех персон по гражданству.
     *
     * @param citizenship гражданство для поиска
     * @return список персон с указанным гражданством
     */
    List<T> findByCitizenship(String citizenship);

    /**
     * Находит всех персон по гражданству с пагинацией.
     *
     * @param citizenship гражданство для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами с указанным гражданством
     */
    Page<T> findByCitizenship(String citizenship, Pageable pageable);

    /**
     * Находит всех персон, чьи ФИО содержат указанный текст (без учета регистра).
     *
     * @param text текст для поиска в ФИО
     * @return список персон, чьи ФИО содержат указанный текст
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "LOWER(CONCAT(p.firstName, ' ', p.lastName, ' ', COALESCE(p.middleName, ''))) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND p.deletedAt IS NULL")
    List<T> findByFullNameContaining(@Param("text") String text);

    /**
     * Находит всех персон, чьи ФИО содержат указанный текст с пагинацией.
     *
     * @param text текст для поиска в ФИО
     * @param pageable параметры пагинации
     * @return страница с персонами, чьи ФИО содержат указанный текст
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "LOWER(CONCAT(p.firstName, ' ', p.lastName, ' ', COALESCE(p.middleName, ''))) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND p.deletedAt IS NULL")
    Page<T> findByFullNameContaining(@Param("text") String text, Pageable pageable);

    /**
     * Находит всех персон, проживающих по указанному адресу.
     *
     * @param address адрес для поиска
     * @return список персон, проживающих по указанному адресу
     */
    List<T> findByAddressContaining(String address);

    /**
     * Находит всех персон, проживающих по указанному адресу с пагинацией.
     *
     * @param address адрес для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами, проживающими по указанному адресу
     */
    Page<T> findByAddressContaining(String address, Pageable pageable);

    /**
     * Находит всех персон указанного возраста.
     *
     * @param age возраст для поиска
     * @return список персон указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(:age = YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) " +
            "AND p.deletedAt IS NULL")
    List<T> findByAge(@Param("age") int age);

    /**
     * Находит всех персон указанного возраста с пагинацией.
     *
     * @param age возраст для поиска
     * @param pageable параметры пагинации
     * @return страница с персонами указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(:age = YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) " +
            "AND p.deletedAt IS NULL")
    Page<T> findByAge(@Param("age") int age, Pageable pageable);

    /**
     * Находит всех персон старше указанного возраста.
     *
     * @param age возраст для фильтрации
     * @return список персон старше указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(:age < YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) " +
            "AND p.deletedAt IS NULL")
    List<T> findByAgeGreaterThan(@Param("age") int age);

    /**
     * Находит всех персон старше указанного возраста с пагинацией.
     *
     * @param age возраст для фильтрации
     * @param pageable параметры пагинации
     * @return страница с персонами старше указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(:age < YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) " +
            "AND p.deletedAt IS NULL")
    Page<T> findByAgeGreaterThan(@Param("age") int age, Pageable pageable);

    /**
     * Находит всех персон младше указанного возраста.
     *
     * @param age возраст для фильтрации
     * @return список персон младше указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(:age > YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) " +
            "AND p.deletedAt IS NULL")
    List<T> findByAgeLessThan(@Param("age") int age);

    /**
     * Находит всех персон младше указанного возраста с пагинацией.
     *
     * @param age возраст для фильтрации
     * @param pageable параметры пагинации
     * @return страница с персонами младше указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(:age > YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) " +
            "AND p.deletedAt IS NULL")
    Page<T> findByAgeLessThan(@Param("age") int age, Pageable pageable);

    /**
     * Находит всех совершеннолетних персон.
     *
     * @return список совершеннолетних персон
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) >= 18 " +
            "AND p.deletedAt IS NULL")
    List<T> findAdults();

    /**
     * Находит всех пенсионеров.
     *
     * @return список пенсионеров
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "((p.gender = 'MALE' AND (YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) >= 65) " +
            "OR (p.gender = 'FEMALE' AND (YEAR(CURRENT_DATE) - YEAR(p.birthDate) - " +
            "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.birthDate) OR " +
            "(MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)) THEN 1 ELSE 0 END) >= 60)) " +
            "AND p.deletedAt IS NULL")
    List<T> findPensioners();

    /**
     * Проверяет существование персоны с указанным email.
     *
     * @param email email для проверки
     * @return true если персона с таким email существует, false в противном случае
     */
    boolean existsByEmail(String email);

    /**
     * Проверяет существование персоны с указанным email (включая удаленные).
     *
     * @param email email для проверки
     * @return true если персона с таким email существует, false в противном случае
     */
    @Query("SELECT COUNT(p) > 0 FROM #{#entityName} p WHERE p.email = :email")
    boolean existsByEmailWithDeleted(@Param("email") String email);

    /**
     * Проверяет существование персоны с указанным ИНН.
     *
     * @param inn ИНН для проверки
     * @return true если персона с таким ИНН существует, false в противном случае
     */
    boolean existsByInn(String inn);

    /**
     * Проверяет существование персоны с указанным номером паспорта.
     *
     * @param passportNumber номер паспорта для проверки
     * @return true если персона с таким номером паспорта существует, false в противном случае
     */
    boolean existsByPassportNumber(String passportNumber);

    /**
     * Проверяет существование персоны с указанной серией и номером паспорта.
     *
     * @param passportSeries серия паспорта
     * @param passportNumber номер паспорта
     * @return true если персона с такими паспортными данными существует, false в противном случае
     */
    boolean existsByPassportSeriesAndPassportNumber(String passportSeries, String passportNumber);

    /**
     * Подсчитывает количество персон указанного пола.
     *
     * @param gender пол для подсчета
     * @return количество персон указанного пола
     */
    @Query("SELECT COUNT(p) FROM #{#entityName} p WHERE p.gender = :gender AND p.deletedAt IS NULL")
    long countByGender(@Param("gender") Gender gender);

    /**
     * Подсчитывает количество персон с указанным семейным положением.
     *
     * @param maritalStatus семейное положение для подсчета
     * @return количество персон с указанным семейным положением
     */
    @Query("SELECT COUNT(p) FROM #{#entityName} p WHERE p.maritalStatus = :maritalStatus AND p.deletedAt IS NULL")
    long countByMaritalStatus(@Param("maritalStatus") MaritalStatus maritalStatus);

    /**
     * Находит персон с неполными данными.
     *
     * @return список персон с неполными данными
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(p.firstName IS NULL OR p.firstName = '' OR " +
            "p.lastName IS NULL OR p.lastName = '' OR " +
            "p.email IS NULL OR p.email = '' OR " +
            "p.birthDate IS NULL) AND p.deletedAt IS NULL")
    List<T> findWithIncompleteData();

    /**
     * Находит персон без контактной информации.
     *
     * @return список персон без контактной информации
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(p.email IS NULL OR p.email = '') AND " +
            "(p.phone IS NULL OR p.phone = '') AND " +
            "p.deletedAt IS NULL")
    List<T> findWithoutContactInfo();
}