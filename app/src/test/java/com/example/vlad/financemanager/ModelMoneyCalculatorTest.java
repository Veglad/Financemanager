package com.example.vlad.financemanager;

import com.example.vlad.financemanager.data.enums.CalculatorOperations;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class ModelMoneyCalculatorTest {

    ModelMoneyCalculator model;

    @Before
    public void initClass(){
        model = new ModelMoneyCalculator();
    }

    @Test
    public void mul_1_5_by_10_eq_15() {
        model.setResultText(new BigDecimal(1.5));
        model.mathOperationBtnClick(CalculatorOperations.MUL);
        model.setResultText(new BigDecimal(10));
        model.calculatePress();

        BigDecimal result = new BigDecimal(model.getResultText());
        assertTrue(result.compareTo( new BigDecimal(15)) == 0);
    }

    @Test
    public void div_25_by_10_eq_2_5() {
        model.setResultText(new BigDecimal(25));
        model.mathOperationBtnClick(CalculatorOperations.DIV);
        model.setResultText(new BigDecimal(10));
        model.calculatePress();

        BigDecimal result = new BigDecimal(model.getResultText());
        assertTrue(result.compareTo( new BigDecimal(2.5)) == 0);
    }

    @Test
    public void div_25_by_0_eq_0() {
        model.setResultText(new BigDecimal(25));
        model.mathOperationBtnClick(CalculatorOperations.DIV);
        model.setResultText(new BigDecimal(0));
        model.calculatePress();

        BigDecimal result = new BigDecimal(model.getResultText());
        assertTrue(result.compareTo( new BigDecimal(0)) == 0);
    }

    @Test
    public void add_2_33_and_17_52_eq_19_85() {
        model.setResultText(new BigDecimal("2.33"));
        model.mathOperationBtnClick(CalculatorOperations.ADD);
        model.setResultText(new BigDecimal("17.52"));
        model.calculatePress();

        BigDecimal result = new BigDecimal(model.getResultText());
        assertTrue(result.compareTo( new BigDecimal("19.85")) == 0);
    }

    @Test
    public void add_2_and_2_mul_2_eq_6() {
        model.setResultText(new BigDecimal(2));
        model.mathOperationBtnClick(CalculatorOperations.ADD);
        model.setResultText(new BigDecimal(2));
        model.mathOperationBtnClick(CalculatorOperations.MUL);
        model.setResultText(new BigDecimal(2));
        model.calculatePress();

        BigDecimal result = new BigDecimal(model.getResultText());
        assertTrue(result.compareTo( new BigDecimal(6)) == 0);
    }

    @Test
    public void num_2dot_dot_incorrect() {
        model.setResultText(new BigDecimal(2));
        model.newResultText('.');
        boolean result =  model.isNewResultCorrect('.');

        assertFalse(result);
    }

    @Test
    public void num_2dot225_incorrect() {
        model.setResultText(new BigDecimal("2.22"));
        boolean result =  model.isNewResultCorrect('5');

        assertFalse(result);
    }

    @Test
    public void num_2dot22_clearLast_eq_2dot2() {
        model.setResultText(new BigDecimal("2.22"));
        model.clearLastSymbol();

        assertFalse(model.getResultText() == "2.2");
    }

    @Test
    public void num_2dot2_clearLast_eq_2dot() {
        model.setResultText(new BigDecimal("2.2"));
        model.clearLastSymbol();

        assertFalse(model.getResultText() == "2.");
    }

    @Test
    public void num_2dot22_clearLast_eq_0() {
        model.setResultText(new BigDecimal("2.22"));
        model.clearNumber();

        BigDecimal result = new BigDecimal(model.getResultText());
        assertTrue(result.compareTo( new BigDecimal("0")) == 0);
    }
}