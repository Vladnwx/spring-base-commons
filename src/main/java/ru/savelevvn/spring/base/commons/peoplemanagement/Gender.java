package ru.savelevvn.spring.base.commons.peoplemanagement;

/**
 * Перечисление, представляющее пол человека.
 * Используется для хранения и обработки информации о гендере в системе управления персоналом.
 *
 * <p>Хранение в базе данных осуществляется в виде строкового значения:
 * <ul>
 *   <li>{@code "MALE"} - мужской пол</li>
 *   <li>{@code "FEMALE"} - женский пол</li>
 * </ul>
 *
 * <p>Пример использования:
 * <pre>
 * {@code
 * Person person = new Person();
 * person.setGender(Gender.MALE);
 * // В базе данных будет сохранено как строка "MALE"
 * }
 * </pre>
 *
 * @since 1.0
 * @see Person
 */
public enum Gender {

    /**
     * Мужской пол.
     * Сохраняется в базе данных как строка "MALE".
     */
    MALE,

    /**
     * Женский пол.
     * Сохраняется в базе данных как строка "FEMALE".
     */
    FEMALE
}