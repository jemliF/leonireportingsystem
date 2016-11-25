/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leoni.mfa.reporting.util;

import java.math.BigDecimal;
import java.util.Comparator;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TableColumn;

/**
 *
 * @author bewa1022
 */
public class BigDecimalColumnComparator implements Comparator<BigDecimal> {

    private final ObjectProperty<TableColumn.SortType> sortTypeProperty;

    public BigDecimalColumnComparator(ObjectProperty<TableColumn.SortType> sortTypeProperty) {
        this.sortTypeProperty = sortTypeProperty;
    }

    public int compare(BigDecimal o1, BigDecimal o2) {
        TableColumn.SortType sortType = sortTypeProperty.get();
        if (sortType == null) {
            return 0;
        }

        if (o1 instanceof BigDecimal) {
            if (sortType == TableColumn.SortType.ASCENDING) {
                return 1;
            } else {
                return -1;
            }
        } else if (o2 instanceof BigDecimal) {
            if (sortType == TableColumn.SortType.ASCENDING) {
                return -1;
            } else {
                return 1;
            }
        }

        return o1.compareTo(o2);
    }
}
