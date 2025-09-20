package ru.savelevvn.spring.base.commons.peoplemanagement.employee;

/**
 * Перечисление, представляющее график работы сотрудника.
 * Хранится в базе данных в виде строкового значения.
 *
 * @since 1.0
 * @see Employee
 */
public enum WorkSchedule {

    /** Полный рабочий день */
    FULL_TIME,

    /** Неполный рабочий день */
    PART_TIME,

    /** Удаленная работа */
    REMOTE,

    /** Гибкий график */
    FLEXIBLE,

    /** Работа в сменах */
    SHIFT
}