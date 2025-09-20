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
 * @since 1.0
 * @see Person
 */
public enum MaritalStatus {

    /**
     * Холост или незамужем.
     * Сохраняется в базе данных как строка "SINGLE".
     */
    SINGLE,

    /**
     * Женат или замужем.
     * Сохраняется в базе данных как строка "MARRIED".
     */
    MARRIED,

    /**
     * В разводе.
     * Сохраняется в базе данных как строка "DIVORCED".
     */
    DIVORCED,

    /**
     * Вдовец или вдова.
     * Сохраняется в базе данных как строка "WIDOWED".
     */
    WIDOWED
}