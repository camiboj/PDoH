package com.tpp.private_doh.dns;

import java.util.List;

public class Response {
    private List<Question> questions;
    private List<Answer> answers;

    public Response(List<Question> questions,
                    List<Answer> answers) {
        this.questions = questions;
        this.answers = answers;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<Answer> getAnswers() {
        return answers;
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

        public int getType() {
            return type;
        }
    }

    static public class Answer {
        private String name;
        private int type;
        private int ttl;
        private String data;

        public Answer(String name, int type, int ttl, String data) {
            this.name = name;
            this.type = type;
            this.ttl = ttl;
            this.data = data;
        }

        public String getName() {
            return name;
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
    }
}
