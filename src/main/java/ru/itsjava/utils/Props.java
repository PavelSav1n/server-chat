package ru.itsjava.utils;

import lombok.SneakyThrows;

import java.util.Properties;

// Класс обёртка, необходимый для подключения настроек для программы и получать значения по ключу
public class Props {
    private final Properties properties;

    @SneakyThrows // для метода load
    public Props (){
        this.properties = new Properties();
        properties.load(Props.class.getClassLoader().getResourceAsStream("application.properties"));
    }

    public String getValue(String key){
        return properties.getProperty(key);
    }
}
