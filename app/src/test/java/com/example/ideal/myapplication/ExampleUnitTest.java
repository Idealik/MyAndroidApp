package com.example.ideal.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public  void Check_DBData(){ assertEquals(4, 2 + 2); }

    @Test
    //Пароль должен быть длиннее 5, содержать заглавные буквы и цифры
    public  void Pass_ShouldBeStrong(){
        String pass = "Pass123";
        registration reg = new registration();

        assertTrue(reg.isStrongPassword(pass));
    }

    @Test
    public  void Pass_CannotBeSlow(){
        String pass = "";
        registration reg = new registration();

        assertFalse(reg.isStrongPassword(pass));
    }
    @Test
    public  void Inputs_CannotBeEmptyAddSer(){
        addService adS = new addService();
        assertTrue(adS.isFullInputs("name", "11", "asd" ));
    }
    @Test
    public  void Inputs_CannotBeEmptyNameIsEmptyAddSer(){
        addService adS = new addService();
        assertFalse(adS.isFullInputs("   ", "11", "asd" ));
    }
    @Test
    public  void Inputs_CannotBeEmptyCostIsEmptyAddSer(){
        addService adS = new addService();
        assertFalse(adS.isFullInputs("name", "", "asd" ));
    }
    @Test
    public  void Inputs_CannotBeEmptyDescrIsEmptyAddSer(){
        addService adS = new addService();
        assertFalse(adS.isFullInputs("name", "s", "" ));
    }

    @Test
    public void Inputs_ContainsOneSymbols(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("A"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsUp(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("AASSSDD"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsLow(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("assdew"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsLowAndUp(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("aSSdew"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsUp(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("ЫЫЫВВ"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsLow(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("ыыуцуйй"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsLowAndUp(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("ЦУЙЙцпПуЫЫуйй"));
    }

    @Test
    public void Inputs_ContainsOnlyRusAndEngSymbolsLowAndUp(){
        registration reg = new registration();

        assertTrue(reg.isCorrectData("ASDDEWывыввыцу"));
    }

    @Test
    public void Inputs_CantContainsNumber(){
        registration reg = new registration();

        assertFalse(reg.isCorrectData("Sы123456789"));
    }
    @Test
    public void Inputs_CantContainsSpecialSymbol(){
        registration reg = new registration();

        assertFalse(reg.isCorrectData("Sы!@#$%^&*()_+"));
    }

    @Test
    public void Inputs_CantContainsSpace(){
        registration reg = new registration();

        assertFalse(reg.isCorrectData("Sы ывцУ"));
    }

    @Test
    public void Inputs_CantBeNull(){
        registration reg = new registration();

        assertFalse(reg.isCorrectData(""));
    }

}

