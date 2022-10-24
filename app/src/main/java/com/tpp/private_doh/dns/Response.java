package com.tpp.private_doh.dns;

import com.tpp.private_doh.util.Requester;

import java.util.List;

public class Response {
    private List<Question> questions;
    private List<Answer> answers;
    private Requester requester;
    private Runnable onWinning;

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

    public void setOnWinning(Runnable f) {
        // Why consumer and not just a function that does not receive anything
        // and also do not return anything? DID NO FIND ONE
        // https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html
        onWinning = f;
    }

    public void setAsWinner() {
        onWinning.run();
    }
}
