package ru.itsjava.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Message {
    private final User from;
    private User to; // Необязательное поле, если рассылаем всем
    private final String text;
}
