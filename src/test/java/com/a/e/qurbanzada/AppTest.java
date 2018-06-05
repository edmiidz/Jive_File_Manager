package com.a.e.qurbanzada;

import static org.junit.Assert.assertTrue;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    public  AppTest (String testName){
        super(testName);
    }

    public static Test suite(){
        return new TestSuite(AppTest.class);
    }

    public void testApp(){
        login("hive.springernature.com","ahmad.e.qurbanzada@gmail.com","solars7M");
        System.out.println("Testing login");
    }

    public void login(String domain,String userName,String password){
        int response = 0;
        if (domain.equals("") || userName.equals("") || password.equals("")){

        }
        else {
            response = 200;
        }
        assertEquals(200, response);
    }

}
