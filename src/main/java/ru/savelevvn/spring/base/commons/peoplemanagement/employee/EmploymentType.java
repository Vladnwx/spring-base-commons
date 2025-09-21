package ru.savelevvn.spring.base.commons.peoplemanagement.employee;

import lombok.Getter;

/**
 * Перечисление, представляющее тип занятости сотрудника.
 * Хранится в базе данных в виде строкового значения.
 *
 * @version 1.0
 * @since 1.0
 * @see Employee
 */
@Getter
public enum EmploymentType {

    /** Штатный сотрудник (постоянное трудоустройство) */
    FULL_TIME("Штатный сотрудник", true, true),

    /** Совместитель (работа на условиях неполного рабочего времени) */
    PART_TIME("Совместитель", true, false),

    /** По контракту (срочный трудовой договор) */
    CONTRACT("По контракту", false, true),

    /** Стажер (временное трудоустройство для получения опыта) */
    INTERN("Стажер", false, false),

    /** Временный сотрудник (временное трудоустройство на определенный срок) */
    TEMPORARY("Временный сотрудник", false, true);

    /**
     * -- GETTER --
     *  Получает описание типа занятости.
     *
     * @return описание типа занятости на русском языке
     */
    private final String description;
    /**
     * -- GETTER --
     *  Проверяет, является ли занятость постоянной.
     *
     * @return true, если это постоянная занятость
     */
    private final boolean isPermanent;
    /**
     * -- GETTER --
     *  Проверяет, является ли занятость оплачиваемой.
     *
     * @return true, если это оплачиваемая занятость
     */
    private final boolean isPaid;

    EmploymentType(String description, boolean isPermanent, boolean isPaid) {
        this.description = description;
        this.isPermanent = isPermanent;
        this.isPaid = isPaid;
    }

    /**
     * Проверяет, является ли тип занятости штатным.
     *
     * @return true, если это штатный сотрудник
     */
    public boolean isFullTimeEmployee() {
        return this == FULL_TIME;
    }

    /**
     * Проверяет, является ли тип занятости временным.
     *
     * @return true, если это временная занятость
     */
    public boolean isTemporary() {
        return this == TEMPORARY || this == CONTRACT || this == INTERN;
    }

    /**
     * Проверяет, является ли тип занятости стажировкой.
     *
     * @return true, если это стажировка
     */
    public boolean isIntern() {
        return this == INTERN;
    }

    /**
     * Проверяет, имеет ли сотрудник право на отпуск.
     *
     * @return true, если сотрудник имеет право на отпуск
     */
    public boolean isEligibleForVacation() {
        return isPermanent() || this == CONTRACT;
    }

    /**
     * Проверяет, имеет ли сотрудник право на социальные гарантии.
     *
     * @return true, если сотрудник имеет право на социальные гарантии
     */
    public boolean isEligibleForBenefits() {
        return isPermanent() || this == CONTRACT;
    }
}