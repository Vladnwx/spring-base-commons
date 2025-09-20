package ru.savelevvn.spring.base.commons.peoplemanagement.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.savelevvn.spring.base.commons.BaseRepository;
import ru.savelevvn.spring.base.commons.peoplemanagement.Person;

import java.io.Serializable;
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
 * </ul>
 *
 * <p>Использует JPA Specification для динамического построения запросов.
 *
 * @param <T> тип сущности, расширяющей Person
 *
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
     * Находит всех персон с указанной фамилией.
     *
     * @param lastName фамилия для поиска
     * @return список персон с указанной фамилией
     */
    List<T> findByLastName(String lastName);

    /**
     * Находит всех персон с указанным именем и фамилией.
     *
     * @param firstName имя для поиска
     * @param lastName фамилия для поиска
     * @return список персон с указанными именем и фамилией
     */
    List<T> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Находит всех персон с указанной датой рождения.
     *
     * @param birthDate дата рождения для поиска
     * @return список персон с указанной датой рождения
     */
    List<T> findByBirthDate(LocalDate birthDate);

    /**
     * Находит всех персон, родившихся в указанный период.
     *
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список персон, родившихся в указанный период
     */
    List<T> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Находит всех персон указанного пола.
     *
     * @param gender пол для фильтрации
     * @return список персон указанного пола
     */
    List<T> findByGender(ru.savelevvn.spring.base.commons.peoplemanagement.Gender gender);

    /**
     * Находит всех персон с указанным семейным положением.
     *
     * @param maritalStatus семейное положение для фильтрации
     * @return список персон с указанным семейным положением
     */
    List<T> findByMaritalStatus(ru.savelevvn.spring.base.commons.peoplemanagement.MaritalStatus maritalStatus);

    /**
     * Находит всех персон по номеру паспорта.
     *
     * @param passportNumber номер паспорта для поиска
     * @return список персон с указанным номером паспорта
     */
    List<T> findByPassportNumber(String passportNumber);

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
     * Находит всех персон, проживающих по указанному адресу.
     *
     * @param address адрес для поиска
     * @return список персон, проживающих по указанному адресу
     */
    List<T> findByAddressContaining(String address);

    /**
     * Находит всех персон указанного возраста.
     *
     * @param age возраст для поиска
     * @return список персон указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "YEAR(CURRENT_DATE) - YEAR(p.birthDate) = :age " +
            "AND (MONTH(CURRENT_DATE) > MONTH(p.birthDate) " +
            "OR (MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) >= DAY(p.birthDate))) " +
            "AND p.deletedAt IS NULL")
    List<T> findByAge(@Param("age") int age);

    /**
     * Находит всех персон старше указанного возраста.
     *
     * @param age возраст для фильтрации
     * @return список персон старше указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(YEAR(CURRENT_DATE) - YEAR(p.birthDate)) > :age " +
            "OR ((YEAR(CURRENT_DATE) - YEAR(p.birthDate)) = :age " +
            "AND (MONTH(CURRENT_DATE) > MONTH(p.birthDate) " +
            "OR (MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) >= DAY(p.birthDate)))) " +
            "AND p.deletedAt IS NULL")
    List<T> findByAgeGreaterThan(@Param("age") int age);

    /**
     * Находит всех персон младше указанного возраста.
     *
     * @param age возраст для фильтрации
     * @return список персон младше указанного возраста
     */
    @Query("SELECT p FROM #{#entityName} p WHERE " +
            "(YEAR(CURRENT_DATE) - YEAR(p.birthDate)) < :age " +
            "OR ((YEAR(CURRENT_DATE) - YEAR(p.birthDate)) = :age " +
            "AND (MONTH(CURRENT_DATE) < MONTH(p.birthDate) " +
            "OR (MONTH(CURRENT_DATE) = MONTH(p.birthDate) AND DAY(CURRENT_DATE) < DAY(p.birthDate)))) " +
            "AND p.deletedAt IS NULL")
    List<T> findByAgeLessThan(@Param("age") int age);

    /**
     * Проверяет существование персоны с указанным email.
     *
     * @param email email для проверки
     * @return true если персона с таким email существует, false в противном случае
     */
    boolean existsByEmail(String email);

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
}