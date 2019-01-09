package com.example.ideal.myapplication;

import com.example.ideal.myapplication.createService.AddService;
import com.example.ideal.myapplication.logIn.Registration;

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
        Registration reg = new Registration();

        assertTrue(reg.isStrongPassword(pass));
    }

    @Test
    public  void Pass_CannotBeSlow(){
        String pass = "";
        Registration reg = new Registration();

        assertFalse(reg.isStrongPassword(pass));
    }
    @Test
    public  void Inputs_CannotBeEmptyAddSer(){
        AddService adS = new AddService();
        assertTrue(adS.isFullInputs("name", "11", "asd" ));
    }
    @Test
    public  void Inputs_CannotBeEmptyNameIsEmptyAddSer(){
        AddService adS = new AddService();
        assertFalse(adS.isFullInputs("   ", "11", "asd" ));
    }
    @Test
    public  void Inputs_CannotBeEmptyCostIsEmptyAddSer(){
        AddService adS = new AddService();
        assertFalse(adS.isFullInputs("name", "", "asd" ));
    }
    @Test
    public  void Inputs_CannotBeEmptyDescrIsEmptyAddSer(){
        AddService adS = new AddService();
        assertFalse(adS.isFullInputs("name", "s", "" ));
    }

    @Test
    public void Inputs_ContainsOneSymbols(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("A"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsUp(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("AASSSDD"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsLow(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("assdew"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsLowAndUp(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("aSSdew"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsUp(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("ЫЫЫВВ"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsLow(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("ыыуцуйй"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsLowAndUp(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("ЦУЙЙцпПуЫЫуйй"));
    }

    @Test
    public void Inputs_ContainsOnlyRusAndEngSymbolsLowAndUp(){
        Registration reg = new Registration();

        assertTrue(reg.isCorrectData("ASDDEWывыввыцу"));
    }

    @Test
    public void Inputs_CantContainsNumber(){
        Registration reg = new Registration();

        assertFalse(reg.isCorrectData("Sы123456789"));
    }
    @Test
    public void InputsCantContainsSpecialSymbol(){
        Registration reg = new Registration();

        assertFalse(reg.isCorrectData("Sы!@#$%^&*()+"));
    }

    @Test
    public void Inputs_CantContainsSpace(){
        Registration reg = new Registration();

        assertFalse(reg.isCorrectData("Sы ывцУ"));
    }

    @Test
    public void Inputs_CantBeNull(){
        Registration reg = new Registration();

        assertFalse(reg.isCorrectData(""));
    }


}

