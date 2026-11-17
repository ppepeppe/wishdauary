package com.wishdiary.domain.english;

import java.time.LocalDate;
import java.util.List;

public final class DailyEnglish {
    public record Line(String sentence, String meaning) {}
    private static final List<Line> LINES = List.of(
        new Line("Hello!", "안녕!"), new Line("Good morning!", "좋은 아침이야!"),
        new Line("Thank you.", "고마워요."), new Line("I am happy.", "나는 행복해요."),
        new Line("I like apples.", "나는 사과를 좋아해요."), new Line("This is my book.", "이것은 내 책이에요."),
        new Line("I can do it!", "나는 할 수 있어요!"), new Line("Let's play!", "같이 놀자!"),
        new Line("It is sunny.", "날씨가 맑아요."), new Line("I love my family.", "나는 우리 가족을 사랑해요."),
        new Line("How are you?", "어떻게 지내니?"), new Line("I am fine.", "나는 잘 지내요."),
        new Line("Good night!", "잘 자!"), new Line("See you tomorrow!", "내일 보자!"),
        new Line("I have a dog.", "나는 강아지를 키워요."), new Line("The cat is cute.", "고양이가 귀여워요."),
        new Line("I like blue.", "나는 파란색을 좋아해요."), new Line("Please help me.", "도와주세요."),
        new Line("You are kind.", "너는 친절해."), new Line("I am hungry.", "나는 배가 고파요."),
        new Line("Let's eat!", "같이 먹자!"), new Line("It is yummy!", "맛있어요!"),
        new Line("I can jump.", "나는 뛰어오를 수 있어요."), new Line("This is my friend.", "이 친구는 내 친구예요."),
        new Line("I like to draw.", "나는 그림 그리기를 좋아해요."), new Line("Wash your hands.", "손을 씻으세요."),
        new Line("I am ready!", "나는 준비됐어요!"), new Line("Have a nice day!", "좋은 하루 보내!"),
        new Line("May I try?", "내가 해 봐도 될까요?"), new Line("You did well!", "잘했어!")
    );
    private DailyEnglish() {}
    public static Line forDate(LocalDate date) { return LINES.get(Math.floorMod(date.toEpochDay(), LINES.size())); }
}
