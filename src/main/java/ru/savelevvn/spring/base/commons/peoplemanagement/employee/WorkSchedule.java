package ru.savelevvn.spring.base.commons.peoplemanagement.employee;

import lombok.Getter;

/**
 * Перечисление, представляющее график работы сотрудника.
 * Хранится в базе данных в виде строкового значения.
 *
 * @version 1.0
 * @since 1.0
 * @see Employee
 */
@Getter
public enum WorkSchedule {

    /** Полный рабочий день (40 часов в неделю) */
    FULL_TIME("Полный рабочий день", 40),

    /** Неполный рабочий день (менее 40 часов в неделю) */
    PART_TIME("Неполный рабочий день", 20),

    /** Удаленная работа (работа из дома) */
    REMOTE("Удаленная работа", 40),

    /** Гибкий график (персональный график работы) */
    FLEXIBLE("Гибкий график", 40),

    /** Работа в сменах (ночная смена, вечерняя смена и т.д.) */
    SHIFT("Работа в сменах", 40);

    /**
     * -- GETTER --
     *  Получает описание графика работы.
     *
     * @return описание графика работы на русском языке
     */
    private final String description;
    /**
     * -- GETTER --
     *  Получает стандартное количество рабочих часов в неделю.
     *
     * @return стандартное количество часов в неделю
     */
    private final int standardHours;

    WorkSchedule(String description, int standardHours) {
        this.description = description;
        this.standardHours = standardHours;
    }

    /**
     * Проверяет, является ли график полным рабочим днем.
     *
     * @return true, если это полный рабочий день
     */
    public boolean isFullTime() {
        return this == FULL_TIME || this == REMOTE || this == FLEXIBLE || this == SHIFT;
    }

    /**
     * Проверяет, является ли график удаленным.
     *
     * @return true, если это удаленная работа
     */
    public boolean isRemote() {
        return this == REMOTE;
    }

    /**
     * Проверяет, является ли график гибким.
     *
     * @return true, если это гибкий график
     */
    public boolean isFlexible() {
        return this == FLEXIBLE;
    }

    /**
     * Проверяет, является ли график сменным.
     *
     * @return true, если это работа в сменах
     */
    public boolean isShift() {
        return this == SHIFT;
    }
}