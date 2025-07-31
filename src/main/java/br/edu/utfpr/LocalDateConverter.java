package br.edu.utfpr;

import com.opencsv.bean.AbstractBeanField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends AbstractBeanField<String, LocalDate> {

    @Override
    protected LocalDate convert(String value) {
        var formattedDate = LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return formattedDate;
    }
}
