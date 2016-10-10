package com.epam.dlab.auth.ldap;


import java.util.*;
import javax.script.*;

import com.epam.dlab.auth.UserInfo;

public class ScriptList {

    public static void main( String[] args ) throws ScriptException, NoSuchMethodException {

        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();

        for (ScriptEngineFactory factory : factories) {

            System.out.println("ScriptEngineFactory Info");

            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();

            System.out.printf("\tScript Engine: %s (%s)%n", engName, engVersion);

            List<String> engNames = factory.getNames();
            for(String name : engNames) {
                System.out.printf("\tEngine Alias: %s%n", name);
            }

            System.out.printf("\tLanguage: %s (%s)%n", langName, langVersion);

        }
        
        ScriptEngine python = mgr.getEngineByName("python");
        ScriptEngine js = mgr.getEngineByName("javascript");
        python.eval("print \"Hello Python!\"");

        js.eval("print('Hello JavaScript!');");

        Invocable ijs = (Invocable) js;
        Invocable ipy = (Invocable) python;
        
        js.eval("var f=function(ui){print(ui);ui.setFirstName(\"Mike\");return ui;};");
        
        Object res = ijs.invokeFunction("f", new UserInfo("test", "pass"));
        System.out.println(res);
        
        python.eval("def f(ui):\n   print ui\n   ui.setLastName(\"Teplitskiy\")\n   return ui\n");
        Object res2 = ipy.invokeFunction("f", new UserInfo("test", "pass"));
        System.out.println(res2);        
     
        
        
    }

}