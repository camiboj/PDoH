package com.tpp.private_doh.doh;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GoogleDohResponse {
    @JsonProperty("Question")
    private List<Question> questions = new ArrayList<>();
    @JsonProperty("Answer")
    private List<Answer> answers = new ArrayList<>();
    public GoogleDohResponse() {
    } // needed by Jackson

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GoogleDohResponse {");
        sb.append("Question=").append(questions);
        sb.append(", Answers=").append(answers);
        sb.append('}');
        return sb.toString();
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    static public class Question {
        private String name;
        private int type;

        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Question{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            sb.append('}');
            return sb.toString();
        }
    }

    static public class Answer {
        private String name;
        private int type;
        @JsonProperty("TTL")
        private int ttl;
        private String data;

        public Answer() {
        } // needed by Jackson

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public int getTtl() {
            return ttl;
        }

        public String getData() {
            return data;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Answer{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            sb.append(", ttl=").append(ttl);
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }
    }
}
