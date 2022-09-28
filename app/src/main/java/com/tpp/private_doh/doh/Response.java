package com.tpp.private_doh.doh;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Response {
    private boolean TC = false;
    private boolean RD = false;
    private boolean RA = false;
    private boolean AD = false;
    private boolean CD = false;

    @JsonProperty("Question")
    private List<Question> questions = new ArrayList<>();
    @JsonProperty("Answer")
    private List<Answer> answers = new ArrayList<>();

    public Response() {
    } // needed by Jackson

    public Response(List<Question> questions,
                    List<Answer> answers) {
        this.questions = questions;
        this.answers = answers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DohResponse {");
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

        public Question(String name, int type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
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

        public Answer(String name, int type, int ttl, String data) {
            this.name = name;
            this.type = type;
            this.ttl = ttl;
            this.data = data;
        }

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
