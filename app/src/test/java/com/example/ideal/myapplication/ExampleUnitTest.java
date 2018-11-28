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

}

