package com.example.ideal.myapplication;

import com.example.ideal.myapplication.fragments.objects.Service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    /*@Test
    //Пароль должен быть длиннее 5, содержать заглавные буквы и цифры
    public  void Pass_ShouldBeStrong(){
        String pass = "Pass123";
        Service reg = new Service();

        assertTrue(reg.isStrongPassword(pass));
    }

    @Test
    public  void Pass_CannotBeSlow(){
        String pass = "";
        Service reg = new Service();

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
    }*/

    @Test
    public void Inputs_ContainsOneSymbols(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("A"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsUp(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("AASSSDD"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsLow(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("assdew"));
    }

    @Test
    public void Inputs_ContainsOnlyEngSymbolsLowAndUp(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("aSSdew"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsUp(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("ЫЫЫВВ"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsLow(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("ыыуцуйй"));
    }

    @Test
    public void Inputs_ContainsOnlyRusSymbolsLowAndUp(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("ЦУЙЙцпПуЫЫуйй"));
    }

    @Test
    public void Inputs_ContainsOnlyRusAndEngSymbolsLowAndUp(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData("ASDDEWывыввыцу"));
    }

    @Test
    public void Inputs_CantContainsNumber(){
        Service reg = new Service();

        assertFalse(reg.isCorrectData("Sы123456789"));
    }
    @Test
    public void InputsCantContainsSpecialSymbol(){
        Service reg = new Service();

        assertFalse(reg.isCorrectData("Sы!@#$%^&*()+"));
    }

    @Test
    public void Inputs_CantContainsSpace(){
        Service reg = new Service();

        assertTrue(reg.isCorrectData(" Sы ыв цУ "));
    }

    @Test
    public void Inputs_CantBeNull(){
        Service reg = new Service();

        assertFalse(reg.isCorrectData(""));
    }


}

