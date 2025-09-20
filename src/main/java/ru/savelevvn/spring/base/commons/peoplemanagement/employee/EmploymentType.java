package ru.savelevvn.spring.base.commons.peoplemanagement.employee;

/**
 * Перечисление, представляющее тип занятости сотрудника.
 * Хранится в базе данных в виде строкового значения.
 *
 * @since 1.0
 * @see Employee
 */
public enum EmploymentType {

    /** Штатный сотрудник */
    FULL_TIME,

    /** Совместитель */
    PART_TIME,

    /** По контракту */
    CONTRACT,

    /** Стажер */
    INTERN,

    /** Временный сотрудник */
    TEMPORARY
}