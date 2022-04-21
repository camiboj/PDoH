package com.mocyx.basic_client.doh;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import java.time.ZonedDateTime; // use latter

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;




@Value
public class GoogleDohAnswer {

    class Question {
        private String name;
        private int type;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Question{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            sb.append('}');
            return sb.toString();
        }
    }

    static class Answer {
        private String name = "";
        private int type = 1;
        private int TTL = 1; // define as time type object(?
        private String data = "1.1.1.1"; // difine as IP class? create ip class?

        public Answer() {} // needed by Jackson


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Answer{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            sb.append(", TTL=").append(TTL);
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }
    }


    public GoogleDohAnswer() {} // needed by Jackson

    private boolean TC = false;
    private boolean RD = false;
    private boolean RA = false;
    private boolean AD = false;
    private boolean CD = false;

    private Question Question = new Question(); // should be name in lowercase but jakson is stupid. fix it

    @JsonProperty("Answer")
    private List<Answer> Answers = new ArrayList<>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GoogleDohAnswer {");
        sb.append("Question=").append(Question);
        sb.append(", Answers=").append(Answers);
        sb.append('}');
        return sb.toString();
    }


}
