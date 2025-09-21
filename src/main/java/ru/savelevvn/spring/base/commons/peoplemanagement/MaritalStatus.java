package ru.savelevvn.spring.base.commons.peoplemanagement;

/**
 * Перечисление, представляющее семейное положение человека.
 * Используется для хранения и обработки информации о семейном статусе в системе управления персоналом.
 *
 * <p>Хранение в базе данных осуществляется в виде строкового значения:
 * <ul>
 *   <li>{@code "SINGLE"} - холост/незамужем</li>
 *   <li>{@code "MARRIED"} - женат/замужем</li>
 *   <li>{@code "DIVORCED"} - в разводе</li>
 *   <li>{@code "WIDOWED"} - вдовец/вдова</li>
 * </ul>
 *
 * <p>Пример использования:
 * <pre>
 * {@code
 * Person person = new Person();
 * person.setMaritalStatus(MaritalStatus.MARRIED);
 * // В базе данных будет сохранено как строка "MARRIED"
 * }
 * </pre>
 *
 * @author Savelev Vladimir
 * @version 1.0
 * @since 1.0
 * @see Person
 */
public enum MaritalStatus {

    /**
     * Холост или незамужем.
     * Сохраняется в базе данных как строка "SINGLE".
     */
    SINGLE("Холост/Незамужем"),

    /**
     * Женат или замужем.
     * Сохраняется в базе данных как строка "MARRIED".
     */
    MARRIED("Женат/Замужем"),

    /**
     * В разводе.
     * Сохраняется в базе данных как строка "DIVORCED".
     */
    DIVORCED("В разводе"),

    /**
     * Вдовец или вдова.
     * Сохраняется в базе данных как строка "WIDOWED".
     */
    WIDOWED("Вдовец/Вдова");

    private final String description;

    MaritalStatus(String description) {
        this.description = description;
    }

    /**
     * Получает описание семейного положения.
     *
     * @return Описание семейного положения на русском языке.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Проверяет, является ли человек женатым/замужним.
     *
     * @return true, если человек женат или замужем.
     */
    public boolean isMarried() {
        return this == MARRIED;
    }

    /**
     * Проверяет, является ли человек одиноким.
     *
     * @return true, если человек холост или незамужем.
     */
    public boolean isSingle() {
        return this == SINGLE;
    }

    /**
     * Проверяет, имеет ли человек опыт брака.
     *
     * @return true, если человек был женат/замужем.
     */
    public boolean hasMarriageExperience() {
        return this == MARRIED || this == DIVORCED || this == WIDOWED;
    }
}