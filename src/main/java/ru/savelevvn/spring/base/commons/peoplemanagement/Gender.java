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
 * @author Savelev Vladimir
 * @version 1.0
 * @since 1.0
 * @see Person
 */
public enum Gender {

    /**
     * Мужской пол.
     * Сохраняется в базе данных как строка "MALE".
     */
    MALE("Мужской"),

    /**
     * Женский пол.
     * Сохраняется в базе данных как строка "FEMALE".
     */
    FEMALE("Женский");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    /**
     * Получает описание пола.
     *
     * @return Описание пола на русском языке.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Получает противоположный пол.
     *
     * @return Противоположный пол.
     */
    public Gender getOpposite() {
        return this == MALE ? FEMALE : MALE;
    }

    /**
     * Проверяет, является ли пол мужским.
     *
     * @return true, если пол мужской.
     */
    public boolean isMale() {
        return this == MALE;
    }

    /**
     * Проверяет, является ли пол женским.
     *
     * @return true, если пол женский.
     */
    public boolean isFemale() {
        return this == FEMALE;
    }
}